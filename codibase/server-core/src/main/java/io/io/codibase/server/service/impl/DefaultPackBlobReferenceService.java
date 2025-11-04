package io.codibase.server.service.impl;

import static io.codibase.server.model.PackBlobReference.PROP_PACK;
import static io.codibase.server.model.PackBlobReference.PROP_PACK_BLOB;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;

import io.codibase.commons.loader.ManagedSerializedForm;
import io.codibase.server.model.Pack;
import io.codibase.server.model.PackBlob;
import io.codibase.server.model.PackBlobReference;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.PackBlobReferenceService;

@Singleton
public class DefaultPackBlobReferenceService extends BaseEntityService<PackBlobReference>
		implements PackBlobReferenceService, Serializable {

	@Transactional
	@Override
	public void create(PackBlobReference blobReference) {
		Preconditions.checkState(blobReference.isNew());
		dao.persist(blobReference);
	}

	@Transactional
	@Override
	public void createIfNotExist(Pack pack, PackBlob packBlob) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PACK, pack));
		criteria.add(Restrictions.eq(PROP_PACK_BLOB, packBlob));
		if (find(criteria) == null) {
			var blobReference = new PackBlobReference();
			blobReference.setPack(pack);
			blobReference.setPackBlob(packBlob);
			dao.persist(blobReference);
		}
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PackBlobReferenceService.class);
	}
	
}
