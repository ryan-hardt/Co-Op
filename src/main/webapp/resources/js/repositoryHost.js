$(function() {
    $('#hostNameRow').hide();
    $('#hostUrlRow').hide();
    $('#namespaceRow').hide();

    //remove fields for github, add fields for gitlab
    $('#repositoryHostType').change(function () {
        const repositoryHostType = $("#repositoryHostType").val();
        if(repositoryHostType === 'github') {
            $('#hostNameLabel').text("GitHub username:");
            $('#hostNameRow').show();
            $('#hostUrlRow').hide();
            $('#namespaceRow').hide();
        } else if(repositoryHostType === 'gitlab') {
            $('#hostNameLabel').text("Repository host name:");
            $('#hostNameRow').show();
            $('#hostUrlRow').show();
            $('#namespaceRow').show();
        }
    });
});

function validateRepository() {
    var errorDiv = document.getElementById("errors");
    let repositoryType = document.addRepositoryHostForm.repositoryHostType.value;

    if(repositoryType === 'github') {
        //must have name
        let name = document.addRepositoryHostForm.repositoryHostName.value;
        if (name === "") {
            errorDiv.innerHTML = "Please specify the GitHub username.";
            errorDiv.style["display"] = "block";
            return false;
        }
        if (name.length > 50) {
            errorDiv.innerHTML = "Your GitHub username can be no more than 50 characters long.";
            errorDiv.style["display"] = "block";
            return false;
        }
        document.addRepositoryHostForm.repositoryHostUrl.value = 'https://github.com/'+name;
    } else if(repositoryType === 'gitlab') {
        //must have name
        let name = document.addRepositoryHostForm.repositoryHostName.value;
        if (name == "") {
            errorDiv.innerHTML = "Your repository host must have a name.";
            errorDiv.style["display"] = "block";
            return false;
        }
        if (name.length > 30) {
            errorDiv.innerHTML = "Your repository host name can be no more than 50 characters long.";
            errorDiv.style["display"] = "block";
            return false;
        }

        //must have url
        let repositoryHostUrl = document.addRepositoryHostForm.repositoryHostUrl.value;
        if (repositoryHostUrl == "") {
            errorDiv.innerHTML = "Your repository host must have a url.";
            errorDiv.style["display"] = "block";
            return false;
        }
    }

    //must have access token
    let accessToken = document.addRepositoryHostForm.repositoryHostAccessToken.value;
    if (accessToken == "") {
        errorDiv.innerHTML = "Your repository host must have an access token.";
        errorDiv.style["display"] = "block";
        return false;
    }

    return true;
}