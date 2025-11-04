package io.codibase.server.git.signatureverification.gpg;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.git.signatureverification.VerificationSuccessful;
import io.codibase.server.model.support.administration.GpgSetting;
import io.codibase.server.web.component.gitsignature.GpgVerificationDetailPanel;
import org.apache.wicket.Component;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

import static io.codibase.server.util.GpgUtils.getEmailAddresses;
import static io.codibase.server.web.translation.Translation._T;

public class GpgVerificationSuccessful implements VerificationSuccessful {
	
	private final GpgSigningKey signingKey;
	
	public GpgVerificationSuccessful(GpgSigningKey signingKey) {
		this.signingKey = signingKey;
	}
	
	@Override
	public Component renderDetail(String componentId, RevObject revObject) {
		var publicKey = signingKey.getPublicKey();
		String message;
		if (signingKey.getEmailAddresses() != null) {
			if (revObject instanceof RevCommit)
				message = _T("Signature verified successfully with committer's GPG key");
			else
				message = _T("Signature verified successfully with tagger's GPG key");
		} else if (getGpgSetting().getTrustedSignatureVerificationKey(publicKey.getKeyID()) != null) {
			message = _T("Signature verified successfully with trusted GPG key");
		} else {
			message = _T("Signature verified successfully with CodiBase GPG key");
		}
		
		return new GpgVerificationDetailPanel(componentId, true, message, 
				publicKey.getKeyID(), getEmailAddresses(publicKey));
	}
	
	private GpgSetting getGpgSetting() {
		return CodiBase.getInstance(SettingService.class).getGpgSetting();
	}
	
}
