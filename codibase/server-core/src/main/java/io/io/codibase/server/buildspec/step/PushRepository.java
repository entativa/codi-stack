package io.codibase.server.buildspec.step;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import io.codibase.commons.bootstrap.SecretMasker;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.commons.utils.command.Commandline;
import io.codibase.commons.utils.command.LineConsumer;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.git.CommandUtils;
import io.codibase.server.git.GitUtils;
import io.codibase.server.model.Project;
import io.codibase.server.persistence.SessionService;

@Editable(order=1080, name="Push to Remote", group=StepGroup.REPOSITORY_SYNC, 
		description="This step pushes current commit to same ref on remote")
public class PushRepository extends SyncRepository {

	private static final long serialVersionUID = 1L;

	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return CodiBase.getInstance(SessionService.class).call(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			var certificateFile = writeCertificate(getCertificate());
			SecretMasker.push(build.getSecretMasker());
			try {
				if (CodiBase.getInstance(ProjectService.class).hasLfsObjects(build.getProject().getId())) {
					Project project = build.getProject();
					Commandline git = CommandUtils.newGit();
					git.workingDir(CodiBase.getInstance(ProjectService.class).getGitDir(project.getId()));
					configureProxy(git, getProxy());
					configureCertificate(git, certificateFile);

					String remoteUrl = getRemoteUrlWithCredential(build);
					AtomicReference<String> remoteCommitId = new AtomicReference<>(null);
					git.addArgs("ls-remote", remoteUrl, "HEAD", build.getRefName());
					git.execute(new LineConsumer() {

						@Override
						public void consume(String line) {
							String refName = line.substring(40).trim();
							if (refName.equals("HEAD")) {
								if (remoteCommitId.get() == null)
									remoteCommitId.set(line.substring(0, 40));
							} else {
								remoteCommitId.set(line.substring(0, 40));
							}
						}

					}, new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.warning(line);
						}

					});

					if (remoteCommitId.get() != null) {
						git.clearArgs();
						configureProxy(git, getProxy());
						configureCertificate(git, certificateFile);
						git.addArgs("fetch", remoteUrl, remoteCommitId.get());
						git.execute(new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.log(line);
							}

						}, new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.warning(line);
							}

						}).checkReturnCode();

						Repository repository = CodiBase.getInstance(ProjectService.class)
								.getRepository(project.getId());
						String mergeBaseId = GitUtils.getMergeBase(repository,
								ObjectId.fromString(remoteCommitId.get()), build.getCommitId()).name();

						if (!mergeBaseId.equals(build.getCommitHash())) {
							String input = String.format("%s %s %s %s\n", build.getRefName(), build.getCommitHash(),
									build.getRefName(), remoteCommitId.get());
							git.clearArgs();
							configureProxy(git, getProxy());
							configureCertificate(git, certificateFile);
							git.addArgs("lfs", "pre-push", remoteUrl, remoteUrl);
							git.execute(new LineConsumer() {

								@Override
								public void consume(String line) {
									logger.log(line);
								}

							}, new LineConsumer() {

								@Override
								public void consume(String line) {
									logger.warning(line);
								}

							}, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))).checkReturnCode();
						}
					} else {
						git.clearArgs();
						configureProxy(git, getProxy());
						configureCertificate(git, certificateFile);
						git.addArgs("lfs", "push", "--all", remoteUrl, build.getCommitHash());
						git.execute(new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.log(line);
							}

						}, new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.warning(line);
							}

						}).checkReturnCode();
					}
				}

				Commandline git = CommandUtils.newGit();
				configureProxy(git, getProxy());
				configureCertificate(git, certificateFile);
				git.workingDir(CodiBase.getInstance(ProjectService.class).getGitDir(build.getProject().getId()));
				git.addArgs("push");
				if (isForce())
					git.addArgs("--force");
				git.addArgs(getRemoteUrlWithCredential(build));
				git.addArgs(build.getCommitHash() + ":" + build.getRefName());

				git.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.log(line);
					}

				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.warning(line);
					}

				}).checkReturnCode();
			} finally {
				SecretMasker.pop();
				if (certificateFile != null)
					FileUtils.deleteFile(certificateFile);
			}
			return new ServerStepResult(true);
		});
	}
	
}
