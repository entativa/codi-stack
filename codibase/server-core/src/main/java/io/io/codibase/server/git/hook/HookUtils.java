package io.codibase.server.git.hook;

import com.google.common.base.Preconditions;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.StringUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.ServerConfig;
import io.codibase.server.service.SettingService;
import io.codibase.server.util.CryptoUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HookUtils {

	public static final String HOOK_TOKEN = CryptoUtils.generateSecret(); 
	
	private static final String gitReceiveHook;
	
	static {
        try (InputStream is = HookUtils.class.getClassLoader().getResourceAsStream("git-receive-hook")) {
        	Preconditions.checkNotNull(is);
            gitReceiveHook = StringUtils.join(IOUtils.readLines(is, Charset.defaultCharset()), "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	public static Map<String, String> getHookEnvs(Long projectId, String principal) {
		ServerConfig serverConfig = CodiBase.getInstance(ServerConfig.class);
		SettingService settingService = CodiBase.getInstance(SettingService.class);
		String hookUrl = "http://localhost:" + serverConfig.getHttpPort();
		String curl = settingService.getSystemSetting().getCurlLocation().getExecutable();
		
		Map<String, String> envs = new HashMap<>();
		
        envs.put("CODIBASE_CURL", curl);
		envs.put("CODIBASE_URL", hookUrl);
		envs.put("CODIBASE_HOOK_TOKEN", HOOK_TOKEN);
		envs.put("CODIBASE_USER_ID", principal);
		envs.put("CODIBASE_REPOSITORY_ID", projectId.toString());
		
        envs.put("GITPLEX_CURL", curl);
		envs.put("GITPLEX_URL", hookUrl);
		envs.put("GITPLEX_USER_ID", principal);
		envs.put("GITPLEX_REPOSITORY_ID", projectId.toString());
		
		return envs;
	}
	
	public static boolean isHookValid(File gitDir, String hookName) {
        File hookFile = new File(gitDir, "hooks/" + hookName);
        if (!hookFile.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(hookFile, Charset.defaultCharset());
			if (!content.contains("CODIBASE_HOOK_TOKEN"))
				return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        if (!hookFile.canExecute())
        	return false;
        
        return true;
	}
	
	public static void checkHooks(File gitDir) {
		if (!isHookValid(gitDir, "pre-receive") 
				|| !isHookValid(gitDir, "post-receive")) {
            File hooksDir = new File(gitDir, "hooks");

            File gitPreReceiveHookFile = new File(hooksDir, "pre-receive");
            FileUtils.writeFile(gitPreReceiveHookFile, String.format(gitReceiveHook, "git-prereceive-callback"));
            gitPreReceiveHookFile.setExecutable(true);
            
            File gitPostReceiveHookFile = new File(hooksDir, "post-receive");
            FileUtils.writeFile(gitPostReceiveHookFile, String.format(gitReceiveHook, "git-postreceive-callback"));
            gitPostReceiveHookFile.setExecutable(true);
        }
	}
	
}
