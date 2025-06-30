import jenkins.model.Jenkins
import jenkins.install.InstallState
import jenkins.install.SetupWizard
import hudson.model.UpdateCenter
import hudson.PluginWrapper
import jenkins.model.JenkinsLocationConfiguration
import hudson.security.*

def instance = Jenkins.getInstance()
def updateCenter = instance.getUpdateCenter()

// Configure Jenkins Security - Admin User Creation
println "Configuring Jenkins security..."

// Get admin credentials from environment variables or use defaults
def adminUsername = System.getenv('JENKINS_ADMIN_USER') ?: 'admin'
def adminPassword = System.getenv('JENKINS_ADMIN_PASSWORD') ?: 'admin123'

println "Creating admin user: ${adminUsername}"

// Set up Hudson's own user database
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(adminUsername, adminPassword)
instance.setSecurityRealm(hudsonRealm)

// Set authorization strategy - full control once logged in
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)  // Disable anonymous read access
instance.setAuthorizationStrategy(strategy)

// Save security configuration
instance.save()

println "Security configuration completed - Admin user '${adminUsername}' created"

// Configure Jenkins URL from environment variable
def jenkinsUrl = System.getenv('JENKINS_URL')
if (jenkinsUrl) {
    println "Configuring Jenkins URL: ${jenkinsUrl}"
    def locationConfig = JenkinsLocationConfiguration.get()
    locationConfig.setUrl(jenkinsUrl)
    locationConfig.save()
    println "Jenkins URL configured successfully"
} else {
    println "JENKINS_URL environment variable not set, skipping URL configuration"
}

// Refresh update center to ensure we have latest plugin information
println "Refreshing update center..."
updateCenter.updateAllSites()

// Wait a bit for update center to refresh
sleep(3000)

// Try to get recommended plugins dynamically, fallback to manual list
def recommendedPlugins = []

try {
    // Try different methods to get recommended plugins
    if (SetupWizard.metaClass.respondsTo(SetupWizard, "getRecommendedPlugins")) {
        recommendedPlugins = SetupWizard.getRecommendedPlugins()
        println "Retrieved ${recommendedPlugins.size()} recommended plugins from SetupWizard"
    } else {
        throw new Exception("SetupWizard.getRecommendedPlugins() not available")
    }
} catch (Exception e) {
    println "Could not retrieve recommended plugins dynamically (${e.message}), using manual list"
    // Fallback to manual list of common recommended plugins
    recommendedPlugins = [
      "ionicons-api", "cloudbees-folder", "antisamy-markup-formatter", "asm-api", "docker-workflow",
      "json-path-api", "structs", "workflow-step-api", "token-macro", "build-timeout", 
      "bouncycastle-api", "credentials", "plain-credentials", "variant", "ssh-credentials", 
      "credentials-binding", "scm-api", "workflow-api", "commons-lang3-api", "timestamper", 
      "caffeine-api", "script-security", "javax-activation-api", "jaxb", "snakeyaml-api", 
      "json-api", "jackson2-api", "commons-text-api", "workflow-support", "plugin-util-api", 
      "font-awesome-api", "bootstrap5-api", "jquery3-api", "echarts-api", "display-url-api", 
      "checks-api", "junit", "matrix-project", "resource-disposer", "ws-cleanup", "ant", 
      "okhttp-api", "durable-task", "workflow-durable-task-step", "workflow-scm-step", "pipeline-rest-api",
      "workflow-cps", "workflow-job", "jakarta-activation-api", "jakarta-mail-api", "pipeline-maven", 
      "apache-httpcomponents-client-4-api", "instance-identity", "mailer", "workflow-basic-steps", 
      "gradle", "pipeline-milestone-step", "pipeline-build-step", "pipeline-groovy-lib", "pipeline-multibranch-defaults",
      "pipeline-stage-step", "joda-time-api", "pipeline-model-api", "pipeline-model-extensions", 
      "branch-api", "workflow-multibranch", "pipeline-stage-tags-metadata", "pipeline-input-step", 
      "pipeline-model-definition", "workflow-aggregator", "jjwt-api", "github-api", "pipeline-stage-view",
      "mina-sshd-api-common", "mina-sshd-api-core", "gson-api", "git-client", "git", "blueocean",
      "github", "github-branch-source", "pipeline-github-lib", "metrics", "pipeline-graph-view", 
      "eddsa-api", "trilead-api", "ssh-slaves", "matrix-auth", "ldap", "email-ext", "pam-auth",
      "theme-manager", "dark-theme"
    ]
}

// Additional plugins to install
def additionalPlugins = ["warnings-ng", "monitoring"]

// Combine all plugins
def allPlugins = []
allPlugins.addAll(recommendedPlugins)
allPlugins.addAll(additionalPlugins)

println "Installing ${allPlugins.size()} plugins (${recommendedPlugins.size()} recommended + ${additionalPlugins.size()} additional)..."

def installJobs = []
def alreadyInstalled = []
def notFound = []

allPlugins.each { pluginName ->
    def plugin = instance.getPluginManager().getPlugin(pluginName)
    if (plugin == null) {
        println "Installing: ${pluginName}"
        def pluginToInstall = updateCenter.getPlugin(pluginName)
        if (pluginToInstall) {
            try {
                def installJob = pluginToInstall.deploy(true)
                installJobs.add([name: pluginName, job: installJob])
            } catch (Exception e) {
                println "Error starting installation of ${pluginName}: ${e.message}"
            }
        } else {
            println "Plugin ${pluginName} not found in update center"
            notFound.add(pluginName)
        }
    } else {
        println "Plugin ${pluginName} already installed (version: ${plugin.getVersion()})"
        alreadyInstalled.add(pluginName)
    }
}

// Wait for all installations to complete
if (installJobs.size() > 0) {
    println "Waiting for ${installJobs.size()} plugin installations to complete..."
    
    def successful = []
    def failed = []
    
    installJobs.each { item ->
        try {
            while (!item.job.isDone()) {
                sleep(1000)
            }
            
            // Check if installation was successful
            if (item.job.get()) {
                println "✓ Successfully installed: ${item.name}"
                successful.add(item.name)
            } else {
                println "✗ Failed to install: ${item.name}"
                failed.add(item.name)
            }
        } catch (Exception e) {
            println "✗ Installation failed for ${item.name}: ${e.message}"
            failed.add(item.name)
        }
    }
    
    // Installation summary
    println "\n=== Installation Summary ==="
    println "Already installed: ${alreadyInstalled.size()} plugins"
    println "Successfully installed: ${successful.size()} plugins"
    println "Failed installations: ${failed.size()} plugins"
    println "Not found in update center: ${notFound.size()} plugins"
    
    if (failed.size() > 0) {
        println "Failed plugins: ${failed.join(', ')}"
    }
    
    if (notFound.size() > 0) {
        println "Not found plugins: ${notFound.join(', ')}"
    }
    
    println "\nAll plugin installations completed"
    
    // Optional: Restart Jenkins if plugins were installed
    if (successful.size() > 0) {
        println "Some plugins may require a restart to be fully functional."
        println "You can restart Jenkins manually or uncomment the restart line below."
        // Uncomment the next line if you want automatic restart
        // instance.restart()
    }
} else {
    println "No plugins needed to be installed"
}
