#!/usr/bin/env groovy

/**
 * Uploads an artifact to the specifed release in github using the github api
 * see: https://developer.github.com/v3/repos/releases/#get-a-release-by-tag-name
 * see: https://developer.github.com/v3/repos/releases/#upload-a-release-asset
 *
 * @param owner - The github organization or user.
 * @param repo - The repo where we want to place the release.
 * @param tagName - The name of the tag/release. Example, `master.alpha.1`
 * @param commitish - The commit hash or branch which we want to tag.
 * @param artifactName - The filename of the artifact we wish to upload.
 * @param githubToken - The Oauth token which has permissions to create a release.
 */
def call(String owner, String repo, String tagName, String commitish, String artifactName, String ArtifactPath, String githubToken) {
    def release = new io.vandorp.GitHubRelease(githubToken: githubToken, owner: owner, repo: repo)
    String releaseId = release.createRelease(tagName, commitish, 'Created from CI', false, false)
    String uploadUrl = release.getUploadUrl(releaseId)
    String responseBody = release.uploadArtifact(uploadUrl, artifactName, ArtifactPath)
}
