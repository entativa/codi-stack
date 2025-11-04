package io.codibase.server.buildspec.job;

import io.codibase.k8shelper.KubernetesHelper;
import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.Build;
import io.codibase.server.util.UrlUtils;

import java.net.MalformedURLException;
import java.net.URL;

import static io.codibase.k8shelper.KubernetesHelper.PLACEHOLDER_PREFIX;
import static io.codibase.k8shelper.KubernetesHelper.PLACEHOLDER_SUFFIX;

public enum JobVariable {

	PROJECT_NAME {

		@Override
		public String getValue(Build build) {
			return build.getProject().getName();
		}

	}, 
	PROJECT_PATH {

		@Override
		public String getValue(Build build) {
			return build.getProject().getPath();
		}

	}, 
	JOB_NAME {

		@Override
		public String getValue(Build build) {
			return build.getJobName();
		}
		
	}, 
	JOB_TOKEN {
		@Override
		public String getValue(Build build) {
			return build.getJobToken();
		}
	},
	REF {

		@Override
		public String getValue(Build build) {
			return build.getRefName();
		}
		
	},
	BRANCH {

		@Override
		public String getValue(Build build) {
			return build.getBranch();
		}
		
	},
	TAG {

		@Override
		public String getValue(Build build) {
			return build.getTag();
		}
		
	},
	COMMIT_HASH {

		@Override
		public String getValue(Build build) {
			return build.getCommitHash();
		}
		
	}, 
	BUILD_NUMBER {

		@Override
		public String getValue(Build build) {
			return String.valueOf(build.getNumber());
		}
		
	}, 
	BUILD_VERSION {

		@Override
		public String getValue(Build build) {
			if (build.getVersion() != null)
				return build.getVersion();
			else
				return PLACEHOLDER_PREFIX + KubernetesHelper.BUILD_VERSION + PLACEHOLDER_SUFFIX;
		}
		
	},
	PULL_REQUEST_NUMBER {

		@Override
		public String getValue(Build build) {
			if (build.getRequest() != null)
				return String.valueOf(build.getRequest().getNumber());
			else
				return null;
		}
		
	},
	ISSUE_NUMBER {
		@Override
		public String getValue(Build build) {
			if (build.getIssue() != null)
				return String.valueOf(build.getIssue().getNumber());
			else
				return null;
		}
	},
	SERVER {
		@Override
		public String getValue(Build build) {
			var serverUrl = CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
			return UrlUtils.getServer(serverUrl);
		}
	},
	SERVER_HOST {
		@Override
		public String getValue(Build build) {
			var serverUrl = CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
			try {
				return new URL(serverUrl).getHost();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	},
	SERVER_URL {
		@Override
		public String getValue(Build build) {
			return CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
		}
	};

	public abstract String getValue(Build build);
	
}
