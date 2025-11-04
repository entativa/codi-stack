package io.codibase.server.web.util.editbean;

import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Markdown;
import io.codibase.server.annotation.WithTime;
import io.codibase.server.annotation.WorkingPeriod;
import io.codibase.server.service.SettingService;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Editable(name="Log Work")
public class IssueWorkBean implements Serializable {
	
	private Integer spentTime = 8 * 60;
	
	private Date startAt = new Date();
	
	private String note;

	@Editable(order=100, name="Add Spent Time", descriptionProvider = "getSpentTimeDescription")
	@WorkingPeriod
	@NotNull(message = "Must not be empty")
	@Min(1)
	public Integer getSpentTime() {
		return spentTime;
	}

	public void setSpentTime(Integer spentTime) {
		this.spentTime = spentTime;
	}

	@SuppressWarnings("unused")
	private static String getSpentTimeDescription() {
		var aggregationLink = CodiBase.getInstance(SettingService.class).getIssueSetting().getTimeTrackingSetting().getAggregationLink();
		if (aggregationLink != null)
			return "Add spent time <b class='text-warning'>only for this issue</b>, not counting '" + aggregationLink + "'";
		else
			return "";
	}
	
	@Editable(order=200, description = "When this work starts")
	@WithTime
	@NotNull
	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	@Editable(order=300, description = "Optionally leave a note")
	@Markdown
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
}
