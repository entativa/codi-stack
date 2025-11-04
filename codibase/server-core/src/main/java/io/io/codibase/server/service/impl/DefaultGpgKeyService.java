package io.codibase.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bouncycastle.openpgp.PGPPublicKey;

import com.google.common.base.Preconditions;

import io.codibase.server.cluster.ClusterService;
import io.codibase.server.event.Listen;
import io.codibase.server.event.entity.EntityPersisted;
import io.codibase.server.event.entity.EntityRemoved;
import io.codibase.server.event.system.SystemStarting;
import io.codibase.server.git.signatureverification.gpg.GpgSigningKey;
import io.codibase.server.model.EmailAddress;
import io.codibase.server.model.GpgKey;
import io.codibase.server.model.User;
import io.codibase.server.persistence.TransactionService;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.EmailAddressService;
import io.codibase.server.service.GpgKeyService;
import io.codibase.server.util.GpgUtils;

@Singleton
public class DefaultGpgKeyService extends BaseEntityService<GpgKey> implements GpgKeyService {

	@Inject
	private TransactionService transactionService;

	@Inject
	private EmailAddressService emailAddressService;

	@Inject
	private ClusterService clusterService;
	
	private volatile Map<Long, Long> entityIds;

    @Listen
    @Sessional
    public void on(SystemStarting event) {
		var hazelcastInstance = clusterService.getHazelcastInstance();
    	entityIds = hazelcastInstance.getMap("gpgKeyEntityIds");
    	var cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("gpgKeyCacheInited");
		clusterService.initWithLead(cacheInited, () -> {
			for (GpgKey key: query()) {
				for (Long keyId: key.getKeyIds())
					entityIds.put(keyId, key.getId());
			}
			return 1L;			
		});
    }
    
    @Transactional
    @Listen
    public void on(EntityRemoved event) {
    	if (event.getEntity() instanceof GpgKey) {
    		List<Long> keyIds = ((GpgKey)event.getEntity()).getKeyIds();
    		transactionService.runAfterCommit(() -> {
				for (var id: keyIds)
					entityIds.remove(id);
			});
    	} else if (event.getEntity() instanceof User) {
    		User user = (User) event.getEntity();
    		Collection<Long> keyIds = new ArrayList<>();
    		for (GpgKey key: user.getGpgKeys()) 
    			keyIds.addAll(key.getKeyIds());
    		transactionService.runAfterCommit(() -> {
				for (var id: keyIds)
					entityIds.remove(id);
			});
    	}
    }
    
    @Transactional
    @Listen
    public void on(EntityPersisted event) {
    	if (event.getEntity() instanceof GpgKey) {
    		GpgKey gpgKey = (GpgKey) event.getEntity();
    		List<Long> keyIds = gpgKey.getKeyIds();
    		Long entityId = gpgKey.getId();
    		transactionService.runAfterCommit(() -> {
				for (Long keyId: keyIds)
					entityIds.put(keyId, entityId);
			});
    	}
    }

    @Sessional
	@Override
	public GpgSigningKey findSigningKey(long keyId) {
    	Long entityId = entityIds.get(keyId);
    	if (entityId != null) {
    		return new GpgSigningKey() {
				
    			private transient Collection<String> emailAddresses;
				
				@Override
				public PGPPublicKey getPublicKey() {
					for (PGPPublicKey publicKey: load(entityId).getPublicKeys()) {
						if (keyId == publicKey.getKeyID())
							return publicKey;
					}
					throw new IllegalStateException();
				}

				@Override
				public Collection<String> getEmailAddresses() {
					if (emailAddresses == null) {
						emailAddresses = new LinkedHashSet<>();
						GpgKey gpgKey = load(entityId);
						for (String value: GpgUtils.getEmailAddresses(gpgKey.getPublicKeys().get(0))) {
							EmailAddress emailAddress = emailAddressService.findByValue(value);
							if (emailAddress != null 
									&& emailAddress.isVerified() 
									&& emailAddress.getOwner().equals(gpgKey.getOwner())) {
								emailAddresses.add(value);
							}
						}
					}
					return emailAddresses;
				}
				
			};
    	} else {
    		return null;
    	}
	}

	@Transactional
	@Override
	public void create(GpgKey gpgKey) {
		Preconditions.checkState(gpgKey.isNew());
		dao.persist(gpgKey);
	}

}
