package io.codibase.server;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Configuration;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.config.ShiroFilterConfiguration;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.server.session.SessionDataStoreFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.type.Type;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.basic.NullConverter;
import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;
import com.thoughtworks.xstream.converters.extended.ISO8601SqlTimestampConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.vladsch.flexmark.util.misc.Extension;

import io.codibase.agent.ExecutorUtils;
import io.codibase.commons.bootstrap.Bootstrap;
import io.codibase.commons.loader.AbstractPlugin;
import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.commons.utils.ExceptionUtils;
import io.codibase.commons.utils.StringUtils;
import io.codibase.k8shelper.KubernetesHelper;
import io.codibase.k8shelper.OsInfo;
import io.codibase.server.annotation.Shallow;
import io.codibase.server.attachment.AttachmentService;
import io.codibase.server.attachment.DefaultAttachmentService;
import io.codibase.server.buildspec.BuildSpecSchemaResource;
import io.codibase.server.buildspec.job.log.instruction.LogInstruction;
import io.codibase.server.cluster.ClusterResource;
import io.codibase.server.codequality.CodeProblemContribution;
import io.codibase.server.codequality.LineCoverageContribution;
import io.codibase.server.commandhandler.ApplyDatabaseConstraints;
import io.codibase.server.commandhandler.BackupDatabase;
import io.codibase.server.commandhandler.CheckDataVersion;
import io.codibase.server.commandhandler.CleanDatabase;
import io.codibase.server.commandhandler.ResetAdminPassword;
import io.codibase.server.commandhandler.RestoreDatabase;
import io.codibase.server.commandhandler.Translate;
import io.codibase.server.commandhandler.Upgrade;
import io.codibase.server.data.DataService;
import io.codibase.server.data.DefaultDataService;
import io.codibase.server.entityreference.DefaultReferenceChangeService;
import io.codibase.server.entityreference.ReferenceChangeService;
import io.codibase.server.event.DefaultListenerRegistry;
import io.codibase.server.event.ListenerRegistry;
import io.codibase.server.exception.handler.ExceptionHandler;
import io.codibase.server.git.GitFilter;
import io.codibase.server.git.GitLfsFilter;
import io.codibase.server.git.GitLocationProvider;
import io.codibase.server.git.GoGetFilter;
import io.codibase.server.git.SshCommandCreator;
import io.codibase.server.git.hook.GitPostReceiveCallback;
import io.codibase.server.git.hook.GitPreReceiveCallback;
import io.codibase.server.git.hook.GitPreReceiveChecker;
import io.codibase.server.git.location.GitLocation;
import io.codibase.server.git.service.DefaultGitService;
import io.codibase.server.git.service.GitService;
import io.codibase.server.git.signatureverification.DefaultSignatureVerificationService;
import io.codibase.server.git.signatureverification.SignatureVerificationService;
import io.codibase.server.git.signatureverification.SignatureVerifier;
import io.codibase.server.jetty.DefaultJettyService;
import io.codibase.server.jetty.DefaultSessionDataStoreFactory;
import io.codibase.server.jetty.JettyService;
import io.codibase.server.job.DefaultJobService;
import io.codibase.server.job.DefaultResourceAllocator;
import io.codibase.server.job.JobService;
import io.codibase.server.job.ResourceAllocator;
import io.codibase.server.job.log.DefaultLogService;
import io.codibase.server.job.log.LogService;
import io.codibase.server.mail.DefaultMailService;
import io.codibase.server.mail.MailService;
import io.codibase.server.markdown.DefaultMarkdownService;
import io.codibase.server.markdown.HtmlProcessor;
import io.codibase.server.markdown.MarkdownService;
import io.codibase.server.model.support.administration.GroovyScript;
import io.codibase.server.model.support.administration.authenticator.Authenticator;
import io.codibase.server.notification.BuildNotificationManager;
import io.codibase.server.notification.CodeCommentNotificationManager;
import io.codibase.server.notification.CommitNotificationManager;
import io.codibase.server.notification.IssueNotificationManager;
import io.codibase.server.notification.PackNotificationManager;
import io.codibase.server.notification.PullRequestNotificationManager;
import io.codibase.server.notification.WebHookManager;
import io.codibase.server.pack.PackFilter;
import io.codibase.server.persistence.DefaultIdService;
import io.codibase.server.persistence.DefaultSessionFactoryService;
import io.codibase.server.persistence.DefaultSessionService;
import io.codibase.server.persistence.DefaultTransactionService;
import io.codibase.server.persistence.HibernateInterceptor;
import io.codibase.server.persistence.IdService;
import io.codibase.server.persistence.PersistListener;
import io.codibase.server.persistence.PrefixedNamingStrategy;
import io.codibase.server.persistence.SessionFactoryService;
import io.codibase.server.persistence.SessionFactoryProvider;
import io.codibase.server.persistence.SessionInterceptor;
import io.codibase.server.persistence.SessionService;
import io.codibase.server.persistence.SessionProvider;
import io.codibase.server.persistence.TransactionInterceptor;
import io.codibase.server.persistence.TransactionService;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.Dao;
import io.codibase.server.persistence.dao.DefaultDao;
import io.codibase.server.persistence.exception.ConstraintViolationExceptionHandler;
import io.codibase.server.rest.DefaultServletContainer;
import io.codibase.server.rest.JerseyConfigurator;
import io.codibase.server.rest.ResourceConfigProvider;
import io.codibase.server.rest.WebApplicationExceptionHandler;
import io.codibase.server.rest.resource.McpHelperResource;
import io.codibase.server.rest.resource.ProjectResource;
import io.codibase.server.search.code.CodeIndexService;
import io.codibase.server.search.code.CodeSearchService;
import io.codibase.server.search.code.DefaultCodeIndexService;
import io.codibase.server.search.code.DefaultCodeSearchService;
import io.codibase.server.search.entitytext.CodeCommentTextService;
import io.codibase.server.search.entitytext.DefaultCodeCommentTextService;
import io.codibase.server.search.entitytext.DefaultIssueTextService;
import io.codibase.server.search.entitytext.DefaultPullRequestTextService;
import io.codibase.server.search.entitytext.IssueTextService;
import io.codibase.server.search.entitytext.PullRequestTextService;
import io.codibase.server.security.BasicAuthenticationFilter;
import io.codibase.server.security.BearerAuthenticationFilter;
import io.codibase.server.security.CodePullAuthorizationSource;
import io.codibase.server.security.DefaultFilterChainResolver;
import io.codibase.server.security.DefaultPasswordService;
import io.codibase.server.security.DefaultRememberMeManager;
import io.codibase.server.security.DefaultShiroFilterConfiguration;
import io.codibase.server.security.DefaultWebSecurityManager;
import io.codibase.server.security.FilterChainConfigurator;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.security.realm.GeneralAuthorizingRealm;
import io.codibase.server.service.AccessTokenAuthorizationService;
import io.codibase.server.service.AccessTokenService;
import io.codibase.server.service.AgentAttributeService;
import io.codibase.server.service.AgentLastUsedDateService;
import io.codibase.server.service.AgentService;
import io.codibase.server.service.AgentTokenService;
import io.codibase.server.service.AlertService;
import io.codibase.server.service.BaseAuthorizationService;
import io.codibase.server.service.BuildDependenceService;
import io.codibase.server.service.BuildLabelService;
import io.codibase.server.service.BuildMetricService;
import io.codibase.server.service.BuildParamService;
import io.codibase.server.service.BuildQueryPersonalizationService;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.CodeCommentService;
import io.codibase.server.service.CodeCommentMentionService;
import io.codibase.server.service.CodeCommentQueryPersonalizationService;
import io.codibase.server.service.CodeCommentReplyService;
import io.codibase.server.service.CodeCommentStatusChangeService;
import io.codibase.server.service.CodeCommentTouchService;
import io.codibase.server.service.CommitQueryPersonalizationService;
import io.codibase.server.service.DashboardGroupShareService;
import io.codibase.server.service.DashboardService;
import io.codibase.server.service.DashboardUserShareService;
import io.codibase.server.service.DashboardVisitService;
import io.codibase.server.service.EmailAddressService;
import io.codibase.server.service.GitLfsLockService;
import io.codibase.server.service.GpgKeyService;
import io.codibase.server.service.GroupAuthorizationService;
import io.codibase.server.service.GroupService;
import io.codibase.server.service.IssueAuthorizationService;
import io.codibase.server.service.IssueChangeService;
import io.codibase.server.service.IssueCommentService;
import io.codibase.server.service.IssueCommentReactionService;
import io.codibase.server.service.IssueCommentRevisionService;
import io.codibase.server.service.IssueDescriptionRevisionService;
import io.codibase.server.service.IssueFieldService;
import io.codibase.server.service.IssueLinkService;
import io.codibase.server.service.IssueMentionService;
import io.codibase.server.service.IssueQueryPersonalizationService;
import io.codibase.server.service.IssueReactionService;
import io.codibase.server.service.IssueScheduleService;
import io.codibase.server.service.IssueService;
import io.codibase.server.service.IssueStateHistoryService;
import io.codibase.server.service.IssueTouchService;
import io.codibase.server.service.IssueVoteService;
import io.codibase.server.service.IssueWatchService;
import io.codibase.server.service.IssueWorkService;
import io.codibase.server.service.IterationService;
import io.codibase.server.service.JobCacheService;
import io.codibase.server.service.LabelSpecService;
import io.codibase.server.service.LinkAuthorizationService;
import io.codibase.server.service.LinkSpecService;
import io.codibase.server.service.MembershipService;
import io.codibase.server.service.PackBlobReferenceService;
import io.codibase.server.service.PackBlobService;
import io.codibase.server.service.PackLabelService;
import io.codibase.server.service.PackQueryPersonalizationService;
import io.codibase.server.service.PackService;
import io.codibase.server.service.PendingSuggestionApplyService;
import io.codibase.server.service.ProjectLabelService;
import io.codibase.server.service.ProjectLastEventDateService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.service.PullRequestAssignmentService;
import io.codibase.server.service.PullRequestChangeService;
import io.codibase.server.service.PullRequestCommentService;
import io.codibase.server.service.PullRequestCommentReactionService;
import io.codibase.server.service.PullRequestCommentRevisionService;
import io.codibase.server.service.PullRequestDescriptionRevisionService;
import io.codibase.server.service.PullRequestLabelService;
import io.codibase.server.service.PullRequestMentionService;
import io.codibase.server.service.PullRequestQueryPersonalizationService;
import io.codibase.server.service.PullRequestReactionService;
import io.codibase.server.service.PullRequestReviewService;
import io.codibase.server.service.PullRequestService;
import io.codibase.server.service.PullRequestTouchService;
import io.codibase.server.service.PullRequestUpdateService;
import io.codibase.server.service.PullRequestWatchService;
import io.codibase.server.service.ReviewedDiffService;
import io.codibase.server.service.RoleService;
import io.codibase.server.service.SettingService;
import io.codibase.server.service.SshKeyService;
import io.codibase.server.service.SsoAccountService;
import io.codibase.server.service.SsoProviderService;
import io.codibase.server.service.StopwatchService;
import io.codibase.server.service.UserAuthorizationService;
import io.codibase.server.service.UserInvitationService;
import io.codibase.server.service.UserService;
import io.codibase.server.service.impl.DefaultAccessTokenAuthorizationService;
import io.codibase.server.service.impl.DefaultAccessTokenService;
import io.codibase.server.service.impl.DefaultAgentAttributeService;
import io.codibase.server.service.impl.DefaultAgentLastUsedDateService;
import io.codibase.server.service.impl.DefaultAgentService;
import io.codibase.server.service.impl.DefaultAgentTokenService;
import io.codibase.server.service.impl.DefaultAlertService;
import io.codibase.server.service.impl.DefaultBaseAuthorizationService;
import io.codibase.server.service.impl.DefaultBuildDependenceService;
import io.codibase.server.service.impl.DefaultBuildLabelService;
import io.codibase.server.service.impl.DefaultBuildMetricService;
import io.codibase.server.service.impl.DefaultBuildParamService;
import io.codibase.server.service.impl.DefaultBuildQueryPersonalizationService;
import io.codibase.server.service.impl.DefaultBuildService;
import io.codibase.server.service.impl.DefaultCodeCommentService;
import io.codibase.server.service.impl.DefaultCodeCommentMentionService;
import io.codibase.server.service.impl.DefaultCodeCommentQueryPersonalizationService;
import io.codibase.server.service.impl.DefaultCodeCommentReplyService;
import io.codibase.server.service.impl.DefaultCodeCommentStatusChangeService;
import io.codibase.server.service.impl.DefaultCodeCommentTouchService;
import io.codibase.server.service.impl.DefaultCommitQueryPersonalizationService;
import io.codibase.server.service.impl.DefaultDashboardGroupShareService;
import io.codibase.server.service.impl.DefaultDashboardService;
import io.codibase.server.service.impl.DefaultDashboardUserShareService;
import io.codibase.server.service.impl.DefaultDashboardVisitService;
import io.codibase.server.service.impl.DefaultEmailAddressService;
import io.codibase.server.service.impl.DefaultGitLfsLockService;
import io.codibase.server.service.impl.DefaultGpgKeyService;
import io.codibase.server.service.impl.DefaultGroupAuthorizationService;
import io.codibase.server.service.impl.DefaultGroupService;
import io.codibase.server.service.impl.DefaultIssueAuthorizationService;
import io.codibase.server.service.impl.DefaultIssueChangeService;
import io.codibase.server.service.impl.DefaultIssueCommentService;
import io.codibase.server.service.impl.DefaultIssueCommentReactionService;
import io.codibase.server.service.impl.DefaultIssueCommentRevisionService;
import io.codibase.server.service.impl.DefaultIssueDescriptionRevisionService;
import io.codibase.server.service.impl.DefaultIssueFieldService;
import io.codibase.server.service.impl.DefaultIssueLinkService;
import io.codibase.server.service.impl.DefaultIssueMentionService;
import io.codibase.server.service.impl.DefaultIssueQueryPersonalizationService;
import io.codibase.server.service.impl.DefaultIssueReactionService;
import io.codibase.server.service.impl.DefaultIssueScheduleService;
import io.codibase.server.service.impl.DefaultIssueService;
import io.codibase.server.service.impl.DefaultIssueStateHistoryService;
import io.codibase.server.service.impl.DefaultIssueTouchService;
import io.codibase.server.service.impl.DefaultIssueVoteService;
import io.codibase.server.service.impl.DefaultIssueWatchService;
import io.codibase.server.service.impl.DefaultIssueWorkService;
import io.codibase.server.service.impl.DefaultIterationService;
import io.codibase.server.service.impl.DefaultJobCacheService;
import io.codibase.server.service.impl.DefaultLabelSpecService;
import io.codibase.server.service.impl.DefaultLinkAuthorizationService;
import io.codibase.server.service.impl.DefaultLinkSpecService;
import io.codibase.server.service.impl.DefaultMembershipService;
import io.codibase.server.service.impl.DefaultPackBlobReferenceService;
import io.codibase.server.service.impl.DefaultPackBlobService;
import io.codibase.server.service.impl.DefaultPackLabelService;
import io.codibase.server.service.impl.DefaultPackQueryPersonalizationService;
import io.codibase.server.service.impl.DefaultPackService;
import io.codibase.server.service.impl.DefaultPendingSuggestionApplyService;
import io.codibase.server.service.impl.DefaultProjectLabelService;
import io.codibase.server.service.impl.DefaultProjectLastEventDateService;
import io.codibase.server.service.impl.DefaultProjectService;
import io.codibase.server.service.impl.DefaultPullRequestAssignmentService;
import io.codibase.server.service.impl.DefaultPullRequestChangeService;
import io.codibase.server.service.impl.DefaultPullRequestCommentService;
import io.codibase.server.service.impl.DefaultPullRequestCommentReactionService;
import io.codibase.server.service.impl.DefaultPullRequestCommentRevisionService;
import io.codibase.server.service.impl.DefaultPullRequestDescriptionRevisionService;
import io.codibase.server.service.impl.DefaultPullRequestLabelService;
import io.codibase.server.service.impl.DefaultPullRequestMentionService;
import io.codibase.server.service.impl.DefaultPullRequestQueryPersonalizationService;
import io.codibase.server.service.impl.DefaultPullRequestReactionService;
import io.codibase.server.service.impl.DefaultPullRequestReviewService;
import io.codibase.server.service.impl.DefaultPullRequestService;
import io.codibase.server.service.impl.DefaultPullRequestTouchService;
import io.codibase.server.service.impl.DefaultPullRequestUpdateService;
import io.codibase.server.service.impl.DefaultPullRequestWatchService;
import io.codibase.server.service.impl.DefaultReviewedDiffService;
import io.codibase.server.service.impl.DefaultRoleService;
import io.codibase.server.service.impl.DefaultSettingService;
import io.codibase.server.service.impl.DefaultSshKeyService;
import io.codibase.server.service.impl.DefaultSsoAccountService;
import io.codibase.server.service.impl.DefaultSsoProviderService;
import io.codibase.server.service.impl.DefaultStopwatchService;
import io.codibase.server.service.impl.DefaultUserAuthorizationService;
import io.codibase.server.service.impl.DefaultUserInvitationService;
import io.codibase.server.service.impl.DefaultUserService;
import io.codibase.server.ssh.CommandCreator;
import io.codibase.server.ssh.DefaultSshAuthenticator;
import io.codibase.server.ssh.DefaultSshService;
import io.codibase.server.ssh.SshAuthenticator;
import io.codibase.server.ssh.SshService;
import io.codibase.server.taskschedule.DefaultTaskScheduler;
import io.codibase.server.taskschedule.TaskScheduler;
import io.codibase.server.updatecheck.DefaultUpdateCheckService;
import io.codibase.server.updatecheck.UpdateCheckService;
import io.codibase.server.util.ScriptContribution;
import io.codibase.server.util.concurrent.BatchWorkExecutionService;
import io.codibase.server.util.concurrent.DefaultBatchWorkExecutionService;
import io.codibase.server.util.concurrent.DefaultWorkExecutionService;
import io.codibase.server.util.concurrent.WorkExecutionService;
import io.codibase.server.util.jackson.ObjectMapperConfigurator;
import io.codibase.server.util.jackson.ObjectMapperProvider;
import io.codibase.server.util.jackson.git.GitObjectMapperConfigurator;
import io.codibase.server.util.jackson.hibernate.HibernateObjectMapperConfigurator;
import io.codibase.server.util.oauth.DefaultOAuthTokenService;
import io.codibase.server.util.oauth.OAuthTokenService;
import io.codibase.server.util.xstream.CollectionConverter;
import io.codibase.server.util.xstream.HibernateProxyConverter;
import io.codibase.server.util.xstream.MapConverter;
import io.codibase.server.util.xstream.ObjectMapperConverter;
import io.codibase.server.util.xstream.ReflectionConverter;
import io.codibase.server.util.xstream.StringConverter;
import io.codibase.server.util.xstream.VersionedDocumentConverter;
import io.codibase.server.validation.MessageInterpolator;
import io.codibase.server.validation.ShallowValidatorProvider;
import io.codibase.server.validation.ValidatorProvider;
import io.codibase.server.web.DefaultUrlService;
import io.codibase.server.web.DefaultWicketFilter;
import io.codibase.server.web.DefaultWicketServlet;
import io.codibase.server.web.ResourcePackScopeContribution;
import io.codibase.server.web.UrlService;
import io.codibase.server.web.WebApplication;
import io.codibase.server.web.avatar.AvatarService;
import io.codibase.server.web.avatar.DefaultAvatarService;
import io.codibase.server.web.component.diff.DiffRenderer;
import io.codibase.server.web.component.markdown.SourcePositionTrackExtension;
import io.codibase.server.web.component.markdown.emoji.EmojiExtension;
import io.codibase.server.web.component.taskbutton.TaskButton;
import io.codibase.server.web.editable.DefaultEditSupportRegistry;
import io.codibase.server.web.editable.EditSupport;
import io.codibase.server.web.editable.EditSupportLocator;
import io.codibase.server.web.editable.EditSupportRegistry;
import io.codibase.server.web.exceptionhandler.PageExpiredExceptionHandler;
import io.codibase.server.web.page.layout.AdministrationSettingContribution;
import io.codibase.server.web.page.project.blob.render.BlobRenderer;
import io.codibase.server.web.page.project.setting.ProjectSettingContribution;
import io.codibase.server.web.upload.DefaultUploadService;
import io.codibase.server.web.upload.UploadService;
import io.codibase.server.web.websocket.AlertEventBroadcaster;
import io.codibase.server.web.websocket.BuildEventBroadcaster;
import io.codibase.server.web.websocket.CodeCommentEventBroadcaster;
import io.codibase.server.web.websocket.CommitIndexedBroadcaster;
import io.codibase.server.web.websocket.DefaultWebSocketService;
import io.codibase.server.web.websocket.IssueEventBroadcaster;
import io.codibase.server.web.websocket.PullRequestEventBroadcaster;
import io.codibase.server.web.websocket.WebSocketService;
import io.codibase.server.xodus.CommitInfoService;
import io.codibase.server.xodus.DefaultCommitInfoService;
import io.codibase.server.xodus.DefaultIssueInfoService;
import io.codibase.server.xodus.DefaultPullRequestInfoService;
import io.codibase.server.xodus.DefaultVisitInfoService;
import io.codibase.server.xodus.IssueInfoService;
import io.codibase.server.xodus.PullRequestInfoService;
import io.codibase.server.xodus.VisitInfoService;
import nl.altindag.ssl.SSLFactory;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class CoreModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(ListenerRegistry.class).to(DefaultListenerRegistry.class);
		bind(JettyService.class).to(DefaultJettyService.class);
		bind(ServletContextHandler.class).toProvider(DefaultJettyService.class);
		
		bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).in(Singleton.class);
		
		bind(ValidatorFactory.class).toProvider(() -> {
			Configuration<?> configuration = Validation
					.byDefaultProvider()
					.configure()
					.messageInterpolator(new MessageInterpolator());
			return configuration.buildValidatorFactory();
		}).in(Singleton.class);

		bind(ValidatorFactory.class).annotatedWith(Shallow.class).toProvider(() -> {
			Configuration<?> configuration = Validation
					.byDefaultProvider()
					.configure()
					.traversableResolver(new TraversableResolver() {

						@Override
						public boolean isReachable(Object traversableObject, Node traversableProperty,
								Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
							return true;
						}
	
						@Override
						public boolean isCascadable(Object traversableObject, Node traversableProperty,
								Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
							return false;
						}
					})					
					.messageInterpolator(new MessageInterpolator());
			return configuration.buildValidatorFactory();
		}).in(Singleton.class);
		
		bind(Validator.class).toProvider(ValidatorProvider.class).in(Singleton.class);
		bind(Validator.class).annotatedWith(Shallow.class).toProvider(ShallowValidatorProvider.class).in(Singleton.class);

		configurePersistence();
		configureSecurity();
		configureRestful();
		configureWeb();
		configureGit();
		configureBuild();

		/*
		 * Declare bindings explicitly instead of using ImplementedBy annotation as
		 * HK2 to guice bridge can only search in explicit bindings in Guice   
		 */
		bind(SshAuthenticator.class).to(DefaultSshAuthenticator.class);
		bind(SshService.class).to(DefaultSshService.class);
		bind(MarkdownService.class).to(DefaultMarkdownService.class);
		bind(SettingService.class).to(DefaultSettingService.class);
		bind(DataService.class).to(DefaultDataService.class);
		bind(TaskScheduler.class).to(DefaultTaskScheduler.class);
		bind(PullRequestCommentService.class).to(DefaultPullRequestCommentService.class);
		bind(CodeCommentService.class).to(DefaultCodeCommentService.class);
		bind(PullRequestService.class).to(DefaultPullRequestService.class);
		bind(PullRequestUpdateService.class).to(DefaultPullRequestUpdateService.class);
		bind(ProjectService.class).to(DefaultProjectService.class);
		bind(ProjectLastEventDateService.class).to(DefaultProjectLastEventDateService.class);
		bind(UserInvitationService.class).to(DefaultUserInvitationService.class);
		bind(PullRequestReviewService.class).to(DefaultPullRequestReviewService.class);
		bind(BuildService.class).to(DefaultBuildService.class);
		bind(BuildDependenceService.class).to(DefaultBuildDependenceService.class);
		bind(JobService.class).to(DefaultJobService.class);
		bind(JobCacheService.class).to(DefaultJobCacheService.class);
		bind(LogService.class).to(DefaultLogService.class);
		bind(MailService.class).to(DefaultMailService.class);
		bind(IssueService.class).to(DefaultIssueService.class);
		bind(IssueFieldService.class).to(DefaultIssueFieldService.class);
		bind(BuildParamService.class).to(DefaultBuildParamService.class);
		bind(UserAuthorizationService.class).to(DefaultUserAuthorizationService.class);
		bind(GroupAuthorizationService.class).to(DefaultGroupAuthorizationService.class);
		bind(PullRequestWatchService.class).to(DefaultPullRequestWatchService.class);
		bind(RoleService.class).to(DefaultRoleService.class);
		bind(CommitInfoService.class).to(DefaultCommitInfoService.class);
		bind(IssueInfoService.class).to(DefaultIssueInfoService.class);
		bind(VisitInfoService.class).to(DefaultVisitInfoService.class);
		bind(BatchWorkExecutionService.class).to(DefaultBatchWorkExecutionService.class);
		bind(WorkExecutionService.class).to(DefaultWorkExecutionService.class);
		bind(GroupService.class).to(DefaultGroupService.class);
		bind(IssueMentionService.class).to(DefaultIssueMentionService.class);
		bind(PullRequestMentionService.class).to(DefaultPullRequestMentionService.class);
		bind(CodeCommentMentionService.class).to(DefaultCodeCommentMentionService.class);
		bind(MembershipService.class).to(DefaultMembershipService.class);
		bind(PullRequestChangeService.class).to(DefaultPullRequestChangeService.class);
		bind(CodeCommentReplyService.class).to(DefaultCodeCommentReplyService.class);
		bind(CodeCommentStatusChangeService.class).to(DefaultCodeCommentStatusChangeService.class);
		bind(AttachmentService.class).to(DefaultAttachmentService.class);
		bind(PullRequestInfoService.class).to(DefaultPullRequestInfoService.class);
		bind(PullRequestNotificationManager.class);
		bind(CommitNotificationManager.class);
		bind(BuildNotificationManager.class);
		bind(PackNotificationManager.class);
		bind(IssueNotificationManager.class);
		bind(CodeCommentNotificationManager.class);
		bind(CodeCommentService.class).to(DefaultCodeCommentService.class);
		bind(AccessTokenService.class).to(DefaultAccessTokenService.class);
		bind(UserService.class).to(DefaultUserService.class);
		bind(IssueWatchService.class).to(DefaultIssueWatchService.class);
		bind(IssueChangeService.class).to(DefaultIssueChangeService.class);
		bind(IssueVoteService.class).to(DefaultIssueVoteService.class);
		bind(IssueWorkService.class).to(DefaultIssueWorkService.class);
		bind(IterationService.class).to(DefaultIterationService.class);
		bind(IssueCommentService.class).to(DefaultIssueCommentService.class);
		bind(IssueQueryPersonalizationService.class).to(DefaultIssueQueryPersonalizationService.class);
		bind(PullRequestQueryPersonalizationService.class).to(DefaultPullRequestQueryPersonalizationService.class);
		bind(CodeCommentQueryPersonalizationService.class).to(DefaultCodeCommentQueryPersonalizationService.class);
		bind(CommitQueryPersonalizationService.class).to(DefaultCommitQueryPersonalizationService.class);
		bind(BuildQueryPersonalizationService.class).to(DefaultBuildQueryPersonalizationService.class);
		bind(PackQueryPersonalizationService.class).to(DefaultPackQueryPersonalizationService.class);
		bind(PullRequestAssignmentService.class).to(DefaultPullRequestAssignmentService.class);
		bind(SshKeyService.class).to(DefaultSshKeyService.class);
		bind(BuildMetricService.class).to(DefaultBuildMetricService.class);
		bind(ReferenceChangeService.class).to(DefaultReferenceChangeService.class);
		bind(GitLfsLockService.class).to(DefaultGitLfsLockService.class);
		bind(IssueScheduleService.class).to(DefaultIssueScheduleService.class);
		bind(LinkSpecService.class).to(DefaultLinkSpecService.class);
		bind(IssueLinkService.class).to(DefaultIssueLinkService.class);
		bind(IssueStateHistoryService.class).to(DefaultIssueStateHistoryService.class);
		bind(LinkAuthorizationService.class).to(DefaultLinkAuthorizationService.class);
		bind(EmailAddressService.class).to(DefaultEmailAddressService.class);
		bind(GpgKeyService.class).to(DefaultGpgKeyService.class);
		bind(IssueTextService.class).to(DefaultIssueTextService.class);
		bind(PullRequestTextService.class).to(DefaultPullRequestTextService.class);
		bind(CodeCommentTextService.class).to(DefaultCodeCommentTextService.class);
		bind(PendingSuggestionApplyService.class).to(DefaultPendingSuggestionApplyService.class);
		bind(IssueAuthorizationService.class).to(DefaultIssueAuthorizationService.class);
		bind(DashboardService.class).to(DefaultDashboardService.class);
		bind(DashboardUserShareService.class).to(DefaultDashboardUserShareService.class);
		bind(DashboardGroupShareService.class).to(DefaultDashboardGroupShareService.class);
		bind(DashboardVisitService.class).to(DefaultDashboardVisitService.class);
		bind(LabelSpecService.class).to(DefaultLabelSpecService.class);
		bind(ProjectLabelService.class).to(DefaultProjectLabelService.class);
		bind(BuildLabelService.class).to(DefaultBuildLabelService.class);
		bind(PackLabelService.class).to(DefaultPackLabelService.class);
		bind(PullRequestLabelService.class).to(DefaultPullRequestLabelService.class);
		bind(IssueTouchService.class).to(DefaultIssueTouchService.class);
		bind(PullRequestTouchService.class).to(DefaultPullRequestTouchService.class);
		bind(CodeCommentTouchService.class).to(DefaultCodeCommentTouchService.class);
		bind(AlertService.class).to(DefaultAlertService.class);
		bind(UpdateCheckService.class).to(DefaultUpdateCheckService.class);
		bind(StopwatchService.class).to(DefaultStopwatchService.class);
		bind(PackService.class).to(DefaultPackService.class);
		bind(PackBlobService.class).to(DefaultPackBlobService.class);
		bind(PackBlobReferenceService.class).to(DefaultPackBlobReferenceService.class);
		bind(AccessTokenAuthorizationService.class).to(DefaultAccessTokenAuthorizationService.class);
		bind(ReviewedDiffService.class).to(DefaultReviewedDiffService.class);
		bind(OAuthTokenService.class).to(DefaultOAuthTokenService.class);
		bind(IssueReactionService.class).to(DefaultIssueReactionService.class);
		bind(IssueCommentReactionService.class).to(DefaultIssueCommentReactionService.class);
		bind(PullRequestReactionService.class).to(DefaultPullRequestReactionService.class);
		bind(PullRequestCommentReactionService.class).to(DefaultPullRequestCommentReactionService.class);
		bind(IssueCommentRevisionService.class).to(DefaultIssueCommentRevisionService.class);
		bind(PullRequestCommentRevisionService.class).to(DefaultPullRequestCommentRevisionService.class);
		bind(IssueDescriptionRevisionService.class).to(DefaultIssueDescriptionRevisionService.class);
		bind(PullRequestDescriptionRevisionService.class).to(DefaultPullRequestDescriptionRevisionService.class);
		bind(SsoProviderService.class).to(DefaultSsoProviderService.class);
		bind(SsoAccountService.class).to(DefaultSsoAccountService.class);
		bind(BaseAuthorizationService.class).to(DefaultBaseAuthorizationService.class);
		
		bind(WebHookManager.class);
		
		contribute(CodePullAuthorizationSource.class, DefaultJobService.class);
        
		bind(CodeIndexService.class).to(DefaultCodeIndexService.class);
		bind(CodeSearchService.class).to(DefaultCodeSearchService.class);

		Bootstrap.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<>()) {

			@Override
			public void execute(Runnable command) {
				try {
					super.execute(SecurityUtils.inheritSubject(command));
				} catch (RejectedExecutionException e) {
					if (!isShutdown())
						throw ExceptionUtils.unchecked(e);
				}
			}

        };

	    bind(ExecutorService.class).toProvider(() -> Bootstrap.executorService).in(Singleton.class);
	    
	    bind(OsInfo.class).toProvider(() -> ExecutorUtils.getOsInfo()).in(Singleton.class);
	    
	    contributeFromPackage(LogInstruction.class, LogInstruction.class);	    
	    
		contribute(CodeProblemContribution.class, (build, blobPath, reportName) -> newArrayList());
	    
		contribute(LineCoverageContribution.class, (build, blobPath, reportName) -> new HashMap<>());
		contribute(AdministrationSettingContribution.class, () -> new ArrayList<>());
		contribute(ProjectSettingContribution.class, () -> new ArrayList<>());
		contribute(GitPreReceiveChecker.class, (project, submitter, refName, oldObjectId, newObjectId) -> null);

		bind(PackFilter.class);
	}
	
	private void configureSecurity() {
		contributeFromPackage(Realm.class, GeneralAuthorizingRealm.class);

		bind(ShiroFilterConfiguration.class).to(DefaultShiroFilterConfiguration.class);
		bind(RememberMeManager.class).to(DefaultRememberMeManager.class);
		bind(WebSecurityManager.class).to(DefaultWebSecurityManager.class);
		bind(FilterChainResolver.class).to(DefaultFilterChainResolver.class);
		bind(BasicAuthenticationFilter.class);
		bind(BearerAuthenticationFilter.class);
		bind(PasswordService.class).to(DefaultPasswordService.class);
		bind(ShiroFilter.class);
		install(new ShiroAopModule());
        contribute(FilterChainConfigurator.class, filterChainManager -> {
			filterChainManager.createChain("/**/info/refs", "noSessionCreation, authcBasic, authcBearer");
			filterChainManager.createChain("/**/git-upload-pack", "noSessionCreation, authcBasic, authcBearer");
			filterChainManager.createChain("/**/git-receive-pack", "noSessionCreation, authcBasic, authcBearer");
		});
        contributeFromPackage(Authenticator.class, Authenticator.class);
		
		bind(SSLFactory.class).toProvider(() -> KubernetesHelper.buildSSLFactory(Bootstrap.getTrustCertsDir())).in(Singleton.class);
	}
	
	private void configureGit() {
		contribute(ObjectMapperConfigurator.class, GitObjectMapperConfigurator.class);
		bind(GitService.class).to(DefaultGitService.class);
		bind(GitLocation.class).toProvider(GitLocationProvider.class);
		bind(GitFilter.class);
		bind(GoGetFilter.class);
		bind(GitLfsFilter.class);
		bind(GitPreReceiveCallback.class);
		bind(GitPostReceiveCallback.class);
		bind(SignatureVerificationService.class).to(DefaultSignatureVerificationService.class);
		contribute(CommandCreator.class, SshCommandCreator.class);
		contributeFromPackage(SignatureVerifier.class, SignatureVerifier.class);
	}
	
	private void configureRestful() {
		bind(ResourceConfig.class).toProvider(ResourceConfigProvider.class).in(Singleton.class);
		bind(ServletContainer.class).to(DefaultServletContainer.class);
		
		contribute(FilterChainConfigurator.class, filterChainManager -> filterChainManager.createChain("/~api/**", "noSessionCreation, authcBasic, authcBearer"));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.packages(ProjectResource.class.getPackage().getName()));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.register(ClusterResource.class));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.register(McpHelperResource.class));
		contribute(JerseyConfigurator.class, resourceConfig -> resourceConfig.register(BuildSpecSchemaResource.class));
	}

	private void configureWeb() {
		bind(WicketServlet.class).to(DefaultWicketServlet.class);
		bind(WicketFilter.class).to(DefaultWicketFilter.class);
		bind(EditSupportRegistry.class).to(DefaultEditSupportRegistry.class);
		bind(WebSocketService.class).to(DefaultWebSocketService.class);
		bind(SessionDataStoreFactory.class).to(DefaultSessionDataStoreFactory.class);

		contributeFromPackage(EditSupport.class, EditSupport.class);
		
		bind(org.apache.wicket.protocol.http.WebApplication.class).to(WebApplication.class);
		bind(Application.class).to(WebApplication.class);
		bind(AvatarService.class).to(DefaultAvatarService.class);
		bind(WebSocketService.class).to(DefaultWebSocketService.class);
		
		contributeFromPackage(EditSupport.class, EditSupportLocator.class);
				
		bind(CommitIndexedBroadcaster.class);
		
		contributeFromPackage(DiffRenderer.class, DiffRenderer.class);
		contributeFromPackage(BlobRenderer.class, BlobRenderer.class);

		contribute(Extension.class, new EmojiExtension());
		contribute(Extension.class, new SourcePositionTrackExtension());
		
		contributeFromPackage(HtmlProcessor.class, HtmlProcessor.class);

		contribute(ResourcePackScopeContribution.class, () -> newArrayList(WebApplication.class));
		
		contributeFromPackage(ExceptionHandler.class, ExceptionHandler.class);
		contributeFromPackage(ExceptionHandler.class, ConstraintViolationExceptionHandler.class);
		contributeFromPackage(ExceptionHandler.class, PageExpiredExceptionHandler.class);
		contributeFromPackage(ExceptionHandler.class, WebApplicationExceptionHandler.class);
		
		bind(UrlService.class).to(DefaultUrlService.class);
		bind(CodeCommentEventBroadcaster.class);
		bind(PullRequestEventBroadcaster.class);
		bind(IssueEventBroadcaster.class);
		bind(BuildEventBroadcaster.class);
		bind(AlertEventBroadcaster.class);
		bind(UploadService.class).to(DefaultUploadService.class);
		
		bind(TaskButton.TaskFutureManager.class);
	}
	
	private void configureBuild() {
		bind(ResourceAllocator.class).to(DefaultResourceAllocator.class);
		bind(AgentService.class).to(DefaultAgentService.class);
		bind(AgentTokenService.class).to(DefaultAgentTokenService.class);
		bind(AgentAttributeService.class).to(DefaultAgentAttributeService.class);
		bind(AgentLastUsedDateService.class).to(DefaultAgentLastUsedDateService.class);
		
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName("determine-build-failure-investigator");
				script.setContent(newArrayList("io.codibase.server.util.ScriptContribution.determineBuildFailureInvestigator()"));
				return script;
			}
			
		});
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName("get-build-number");
				script.setContent(newArrayList("io.codibase.server.util.ScriptContribution.getBuildNumber()"));
				return script;
			}
			
		});
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName("get-current-user");
				script.setContent(newArrayList("io.codibase.server.util.ScriptContribution.getCurrentUser()"));
				return script;
			}

		});
	}
	
	private void configurePersistence() {
		bind(DataService.class).to(DefaultDataService.class);
		
		bind(Session.class).toProvider(SessionProvider.class);
		bind(EntityManager.class).toProvider(SessionProvider.class);
		bind(SessionFactory.class).toProvider(SessionFactoryProvider.class);
		bind(EntityManagerFactory.class).toProvider(SessionFactoryProvider.class);
		bind(SessionFactoryService.class).to(DefaultSessionFactoryService.class);
		
	    contribute(ObjectMapperConfigurator.class, HibernateObjectMapperConfigurator.class);
	    
		bind(Interceptor.class).to(HibernateInterceptor.class);
		bind(PhysicalNamingStrategy.class).toInstance(new PrefixedNamingStrategy("o_"));
		
		bind(SessionService.class).to(DefaultSessionService.class);
		bind(TransactionService.class).to(DefaultTransactionService.class);
		bind(IdService.class).to(DefaultIdService.class);
		bind(Dao.class).to(DefaultDao.class);
		
	    TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
	    requestInjection(transactionInterceptor);
	    
	    bindInterceptor(Matchers.any(), new AbstractMatcher<AnnotatedElement>() {

			@Override
			public boolean matches(AnnotatedElement element) {
				return element.isAnnotationPresent(Transactional.class) && !((Method) element).isSynthetic();
			}
	    	
	    }, transactionInterceptor);
	    
	    SessionInterceptor sessionInterceptor = new SessionInterceptor();
	    requestInjection(sessionInterceptor);
	    
	    bindInterceptor(Matchers.any(), new AbstractMatcher<AnnotatedElement>() {

			@Override
			public boolean matches(AnnotatedElement element) {
				return element.isAnnotationPresent(Sessional.class) && !((Method) element).isSynthetic();
			}
	    	
	    }, sessionInterceptor);
	    
	    contribute(PersistListener.class, new PersistListener() {
			
			@Override
			public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
					throws CallbackException {
				return false;
			}
			
			@Override
			public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
					throws CallbackException {
				return false;
			}
			
			@Override
			public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
					String[] propertyNames, Type[] types) throws CallbackException {
				return false;
			}
			
			@Override
			public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
					throws CallbackException {
			}

		});
	    
		bind(XStream.class).toProvider(new com.google.inject.Provider<XStream>() {

			@SuppressWarnings("rawtypes")
			@Override
			public XStream get() {
				ReflectionProvider reflectionProvider = JVM.newReflectionProvider();
				XStream xstream = new XStream(reflectionProvider) {

					@Override
					protected MapperWrapper wrapMapper(MapperWrapper next) {
						return new MapperWrapper(next) {
							
							@Override
							public boolean shouldSerializeMember(Class definedIn, String fieldName) {
								Field field = reflectionProvider.getField(definedIn, fieldName);
								
								return field.getAnnotation(XStreamOmitField.class) == null 
										&& field.getAnnotation(Transient.class) == null 
										&& field.getAnnotation(OneToMany.class) == null 
										&& (field.getAnnotation(OneToOne.class) == null || field.getAnnotation(JoinColumn.class) != null)  
										&& field.getAnnotation(Version.class) == null;
							}
							
							@Override
							public String serializedClass(Class type) {
								if (type == null)
									return super.serializedClass(type);
								else if (type == PersistentBag.class)
									return super.serializedClass(ArrayList.class);
								else if (type.getName().contains("$HibernateProxy$"))
									return StringUtils.substringBefore(type.getName(), "$HibernateProxy$");
								else
									return super.serializedClass(type);
							}
							
						};
					}
					
				};
				xstream.allowTypesByWildcard(new String[] {"**"});				
				
				// register NullConverter as highest; otherwise NPE when unmarshal a map 
				// containing an entry with value set to null.
				xstream.registerConverter(new NullConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new StringConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new VersionedDocumentConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new HibernateProxyConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new CollectionConverter(xstream.getMapper()), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new MapConverter(xstream.getMapper()), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new ObjectMapperConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new ISO8601DateConverter(), XStream.PRIORITY_VERY_HIGH);
				xstream.registerConverter(new ISO8601SqlTimestampConverter(), XStream.PRIORITY_VERY_HIGH); 
				xstream.registerConverter(new ReflectionConverter(xstream.getMapper(), xstream.getReflectionProvider()), 
						XStream.PRIORITY_VERY_LOW);
				xstream.autodetectAnnotations(true);
				return xstream;
			}
			
		}).in(Singleton.class);
	}
	
	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		if (Bootstrap.command != null) {
			if (RestoreDatabase.COMMAND.equals(Bootstrap.command.getName()))
				return RestoreDatabase.class;
			else if (ApplyDatabaseConstraints.COMMAND.equals(Bootstrap.command.getName()))
				return ApplyDatabaseConstraints.class;
			else if (BackupDatabase.COMMAND.equals(Bootstrap.command.getName()))
				return BackupDatabase.class;
			else if (CheckDataVersion.COMMAND.equals(Bootstrap.command.getName()))
				return CheckDataVersion.class;
			else if (Upgrade.COMMAND.equals(Bootstrap.command.getName()))
				return Upgrade.class;
			else if (CleanDatabase.COMMAND.equals(Bootstrap.command.getName()))
				return CleanDatabase.class;
			else if (ResetAdminPassword.COMMAND.equals(Bootstrap.command.getName()))
				return ResetAdminPassword.class;
			else if (Translate.COMMAND.equals(Bootstrap.command.getName()))
				return Translate.class;
			else
				throw new RuntimeException("Unrecognized command: " + Bootstrap.command.getName());
		} else {
			return CodiBase.class;
		}		
	}

}
