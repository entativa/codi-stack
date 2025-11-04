package io.codibase.server.web.page.project.blob.render.commitoption;

import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import io.codibase.commons.utils.ExceptionUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.git.BlobChange;
import io.codibase.server.git.BlobContent;
import io.codibase.server.git.BlobEdits;
import io.codibase.server.git.BlobIdent;
import io.codibase.server.git.GitUtils;
import io.codibase.server.git.exception.NotTreeException;
import io.codibase.server.git.exception.ObjectAlreadyExistsException;
import io.codibase.server.git.exception.ObsoleteCommitException;
import io.codibase.server.git.service.GitService;
import io.codibase.server.git.service.PathChange;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.FileExtension;
import io.codibase.server.util.Provider;
import io.codibase.server.util.diff.WhitespaceOption;
import io.codibase.server.web.ajaxlistener.ConfirmLeaveListener;
import io.codibase.server.web.ajaxlistener.TrackViewStateListener;
import io.codibase.server.web.component.diff.blob.BlobDiffPanel;
import io.codibase.server.web.component.diff.revision.DiffViewMode;
import io.codibase.server.web.component.link.ViewStateAwareAjaxLink;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.page.project.blob.RevisionResolved;
import io.codibase.server.web.page.project.blob.navigator.BlobNameChanging;
import io.codibase.server.web.page.project.blob.render.BlobRenderContext;
import io.codibase.server.web.page.project.blob.render.BlobRenderContext.Mode;

public class CommitOptionPanel extends Panel {

	private final BlobRenderContext context;
	
	private final Provider<byte[]> newContentProvider;
	
	private CommitMessageBean commitMessageBean = new CommitMessageBean();
	
	private BlobChange changesOfOthers;
	
	private boolean contentModified;
	
	private Set<String> oldPaths;
	
	private Form<?> form;
	
	public CommitOptionPanel(String id, BlobRenderContext context, @Nullable Provider<byte[]> newContentProvider) {
		super(id);

		this.context = context;
		this.newContentProvider = newContentProvider;

		oldPaths = new HashSet<>();
		String oldPath = getOldPath();
		if (oldPath != null)
			oldPaths.add(oldPath);
	}

	@Nullable
	private String getOldPath() {
		return context.getMode()!=Mode.ADD? context.getBlobIdent().path: null;
	}
	
	public String getDefaultCommitMessage() {
		String oldPath = getOldPath();
		String oldName;
		if (oldPath != null && oldPath.contains("/"))
			oldName = StringUtils.substringAfterLast(oldPath, "/");
		else
			oldName = oldPath;
		
		String commitMessage;
		if (newContentProvider == null) { 
			commitMessage = "Delete " + oldName;
		} else {
			String newPath = context.getNewPath();

			String newName;
			if (newPath != null && newPath.contains("/"))
				newName = StringUtils.substringAfterLast(newPath, "/");
			else
				newName = newPath;
			
			if (oldPath == null) {
				if (newName != null)
					commitMessage = MessageFormat.format(_T("Add {0}"), newName);
				else
					commitMessage = _T("Add new file");
			} else if (oldPath.equals(newPath)) {
				commitMessage = MessageFormat.format(_T("Edit {0}"), oldName);
			} else {
				commitMessage = MessageFormat.format(_T("Rename {0}"), oldName);
			}
		}
		if (context.getProject().getBranchProtection(context.getBlobIdent().revision, SecurityUtils.getUser()).isEnforceConventionalCommits())
			commitMessage = "chore: " + commitMessage;
		return commitMessage;
	}
	
	private GitService getGitService() {
		return CodiBase.getInstance(GitService.class);
	}
	
	private void newChangesOfOthersContainer(@Nullable AjaxRequestTarget target) {
		Component changesOfOthersContainer;
		if (changesOfOthers != null) 
			changesOfOthersContainer = new BlobDiffPanel("changesOfOthers", changesOfOthers, DiffViewMode.UNIFIED, null);
		else 
			changesOfOthersContainer = new WebMarkupContainer("changesOfOthers").setVisible(false);
		if (target != null) {
			form.replace(changesOfOthersContainer);
			target.add(form);
			if (changesOfOthers != null) {
				String script = String.format("$('#%s .commit-option input[type=submit]').val('Commit and overwrite');", 
						getMarkupId());
				target.appendJavaScript(script);
			}
		} else {
			form.add(changesOfOthersContainer);		
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);

		form.add(new FencedFeedbackPanel("feedback", form));
		newChangesOfOthersContainer(null);
		form.add(BeanContext.edit("commitMessage", commitMessageBean));

		AjaxButton saveButton = new AjaxButton("save") {

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);

				attributes.getAjaxCallListeners().add(new TrackViewStateListener(true));
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				if (!isBlobModified())
					tag.put("disabled", "disabled");
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (save(target)) {
					String script = String.format(""
							+ "$('#%s').attr('disabled', 'disabled').val('" + _T("Please wait...") + "');"
							+ "codibase.server.form.markClean($('form'));", getMarkupId());
					target.appendJavaScript(script);
				} 
			}
			
		};
		saveButton.setOutputMarkupId(true);
		form.add(saveButton);
		
		form.add(new ViewStateAwareAjaxLink<Void>("cancel", true) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(newContentProvider == null);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				context.onModeChange(target, Mode.VIEW, null);
			}
			
		});

		setOutputMarkupId(true);
	}
	
	private boolean isBlobModified() {
		return !context.getBlobIdent().isFile() 
				|| context.getMode() == Mode.DELETE
				|| contentModified 
				|| !Objects.equal(context.getBlobIdent().path, context.getNewPath());
	}
	
	private boolean save(AjaxRequestTarget target) {
		changesOfOthers = null;
		
		if (newContentProvider != null && StringUtils.isBlank(context.getNewPath())) {
			form.error("Please specify file name.");
			target.add(form);
			return false;
		} else {
			User user = SecurityUtils.getAuthUser();
			String commitMessage = commitMessageBean.getCommitMessage();
			if (commitMessage == null)
				commitMessage = getDefaultCommitMessage();
			var branchProtection = context.getProject().getBranchProtection(context.getBlobIdent().revision, user);
			var errorMessage = branchProtection.checkCommitMessage(commitMessage, false);
			if (errorMessage != null) {
				form.error(errorMessage);
				target.add(form);
				return false;
			}
			String revision = context.getBlobIdent().revision;
			ObjectId prevCommitId;
			if (revision != null)
				prevCommitId = context.getProject().getObjectId(revision, true);
			else
				prevCommitId = ObjectId.zeroId();

			if (revision == null)
				revision = "main";
			
			String refName = GitUtils.branch2ref(revision);
			
			ObjectId newCommitId = null;
			while(newCommitId == null) {
				try {
					Map<String, BlobContent> newBlobs = new HashMap<>();
					if (newContentProvider != null) {
						String newPath = context.getNewPath();
						var blobType = FileExtension.getExtension(newPath);

						var disallowedFileTypes = context.getProject().getBranchProtection(revision, user).getDisallowedFileTypes();
						if (disallowedFileTypes.stream().anyMatch(type -> type.equalsIgnoreCase(blobType))) {
							form.error(MessageFormat.format(_T("Not allowed file type: {0}"), blobType));
							target.add(form);
							return false;
						}
			
						if (context.getProject().isReviewRequiredForModification(user, revision, newPath)) {
							form.error(_T("Review required for this change. Please submit pull request instead"));
							target.add(form);
							return false;
						} else if (context.getProject().isBuildRequiredForModification(user, revision, newPath)) {
							form.error(_T("Build required for this change. Please submit pull request instead"));
							target.add(form);
							return false;
						} else if (context.getProject().isCommitSignatureRequiredButNoSigningKey(user, revision)) {
							form.error(_T("Signature required for this change, but no signing key is specified"));
							target.add(form);
							return false;
						}
						
						int mode;
						if (context.getBlobIdent().isFile())
							mode = context.getBlobIdent().mode;
						else
							mode = FileMode.REGULAR_FILE.getBits();
						newBlobs.put(context.getNewPath(), new BlobContent(newContentProvider.get(), mode));
					}

					newCommitId = getGitService().commit(context.getProject(), 
							new BlobEdits(oldPaths, newBlobs), refName, prevCommitId, prevCommitId, 
							user.asPerson(), commitMessage, false);
				} catch (Exception e) {
					ObjectAlreadyExistsException objectAlreadyExistsException = 
							ExceptionUtils.find(e, ObjectAlreadyExistsException.class);
					NotTreeException notTreeException = ExceptionUtils.find(e, NotTreeException.class);
					ObsoleteCommitException obsoleteCommitException = 
							ExceptionUtils.find(e, ObsoleteCommitException.class);
					
					if (objectAlreadyExistsException != null) {
						form.error(_T("A path with same name already exists.Please choose a different name and try again."));
						target.add(form);
						break;
					} else if (notTreeException != null) {
						form.error(_T("A file exists where you’re trying to create a subdirectory. Choose a new path and try again.."));
						target.add(form);
						break;
					} else if (obsoleteCommitException != null) {
						send(this, Broadcast.BUBBLE, new RevisionResolved(target, obsoleteCommitException.getOldCommitId()));
						ObjectId lastPrevCommitId = prevCommitId;
						prevCommitId = obsoleteCommitException.getOldCommitId();
						if (!oldPaths.isEmpty()) {
							String path = oldPaths.iterator().next();
							PathChange pathChange = getGitService().getPathChange(context.getProject(), 
									lastPrevCommitId, prevCommitId, path);
							Preconditions.checkNotNull(pathChange);
							if (!pathChange.getOldObjectId().equals(pathChange.getNewObjectId()) 
									|| pathChange.getOldMode() != pathChange.getNewMode()) {
								// mark changed if original file exists and content or mode has been modified
								// by others
								if (pathChange.getNewObjectId().equals(ObjectId.zeroId())) {
									if (newContentProvider != null) {
										oldPaths.clear();
										changesOfOthers = getBlobChange(path, pathChange, lastPrevCommitId, prevCommitId);
										form.warn(_T("Someone made below change since you started editing"));
										break;
									} else {
										newCommitId = obsoleteCommitException.getOldCommitId();
									}
								} else {
									changesOfOthers = getBlobChange(path, pathChange, lastPrevCommitId, prevCommitId);
									form.warn(_T("Someone made below change since you started editing"));
									break;
								}
							} 
						}
					} else {
						throw ExceptionUtils.unchecked(e);
					}
				}
			}
			if (newCommitId != null) {
				context.onCommitted(target, newCommitId);
				target.appendJavaScript("$(window).resize();");
				return true;
			} else {
				newChangesOfOthersContainer(target);
				return false;
			}
		}
	}
	
	private BlobChange getBlobChange(String path, PathChange pathChange, 
			ObjectId oldCommitId, ObjectId newCommitId) {
		DiffEntry.ChangeType changeType = DiffEntry.ChangeType.MODIFY;
		BlobIdent oldBlobIdent;
		if (!pathChange.getOldObjectId().equals(ObjectId.zeroId())) {
			oldBlobIdent = new BlobIdent(oldCommitId.name(), path, pathChange.getOldMode());
		} else {
			oldBlobIdent = new BlobIdent(oldCommitId.name(), null, FileMode.TREE.getBits());
			changeType = DiffEntry.ChangeType.ADD;
		}
		
		BlobIdent newBlobIdent;
		if (!pathChange.getNewObjectId().equals(ObjectId.zeroId())) {
			newBlobIdent = new BlobIdent(newCommitId.name(), path, pathChange.getNewMode());
		} else {
			newBlobIdent = new BlobIdent(newCommitId.name(), null, FileMode.TREE.getBits());
			changeType = DiffEntry.ChangeType.DELETE;
		}
		
		return new BlobChange(changeType, oldBlobIdent, newBlobIdent, WhitespaceOption.IGNORE_TRAILING) {

			@Override
			public Project getProject() {
				return context.getProject();
			}

		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CommitOptionResourceReference()));
	}
	
	public void onContentChange(IPartialPageRequestHandler partialPageRequestHandler) {
		Preconditions.checkNotNull(newContentProvider);
		
		if (context.getMode() == Mode.EDIT) {
			contentModified = !Arrays.equals(
					newContentProvider.get(), 
					context.getProject().getBlob(context.getBlobIdent(), true).getBytes());
		} else {
			contentModified = newContentProvider.get().length != 0;
		}
		onBlobChange(partialPageRequestHandler);
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof BlobNameChanging) {
			BlobNameChanging payload = (BlobNameChanging) event.getPayload();
			onBlobChange(payload.getHandler());
		}
	}

	private void onBlobChange(IPartialPageRequestHandler partialPageRequestHandler) {
		String script = String.format("codibase.server.commitOption.onBlobChange('%s', %b);", 
				getMarkupId(), isBlobModified());
		partialPageRequestHandler.appendJavaScript(script);
	}
	
}
