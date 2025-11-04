package io.codibase.server.web.page.project.commits;

import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.web.util.WicketUtils;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Editable
public class BranchChoiceBean implements Serializable {

    private String branch;

    @Editable
    @ChoiceProvider("getBranchChoices")
    @OmitName
    @NotEmpty
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @SuppressWarnings("unused")
    private static List<String> getBranchChoices() {
        CommitDetailPage page = (CommitDetailPage) WicketUtils.getPage();
        return page.getOperateBranches();
    }

}
