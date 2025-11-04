package io.codibase.server.web.component.issue.progress;

import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.WorkingPeriod;
import io.codibase.server.service.SettingService;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static io.codibase.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;

@Editable(name="Edit Estimated Time")
public class EstimatedTimeEditBean implements Serializable {
	
	private Integer estimatedTime;

	@Editable(descriptionProvider="getEstimatedTimeDescription")
	@OmitName
	@WorkingPeriod
	@NotNull(message = "Must not be empty")
	@Min(1)
	public Integer getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(Integer estimatedTime) {
		this.estimatedTime = estimatedTime;
	}
	
	@SuppressWarnings("unused")
	private static String getEstimatedTimeDescription() {
		var aggregationLink = CodiBase.getInstance(SettingService.class).getIssueSetting().getTimeTrackingSetting().getAggregationLink();
		if (aggregationLink != null)
			return MessageFormat.format(_T("Specify estimated time <b class='text-warning'>only for this issue</b>, not counting \"{0}\""), aggregationLink);
		else 
			return "";
	}
}
