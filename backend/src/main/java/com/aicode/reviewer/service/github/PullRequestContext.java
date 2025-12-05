package com.aicode.reviewer.service.github;

public record PullRequestContext(
        Long pullRequestId,
        String repoOwner,
        String repoName,
        Integer prNumber,
        String title,
        String author
) {
    public String repoSlug() {
        return repoOwner + "/" + repoName;
    }
}
