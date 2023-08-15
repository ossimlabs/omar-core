package omar.core.plugin

import grails.plugins.*

class OmarCorePluginGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
       def grailsVersion = "6.0.0  > *"
       // resources that are excluded from plugin packaging
       def pluginExcludes = [
           "grails-app/views/error.gsp"
       ]

       // TODO Fill in these fields
       def title = "Omar -core -plugin" // Headline display name of the plugin
       def author = "Your name"
       def authorEmail = ""
       def description = '''\
   Brief summary/description of the plugin.
   '''
       def profiles = ['web']
   // URL to the plugin's documentation
       def documentation = "https://grails.github.io/OmarCorePlugin/"

       // Extra (optional) plugin metadata

       // License: one of 'APACHE', 'GPL2', 'GPL3'
   //    def license = "APACHE"

       // Details of company behind the plugin (if there is one)
   //    def organization = [ name: "My Company", url: "https://www.my-company.com/" ]

       // Any additional developers beyond the author specified above.
   //    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

       // Location of the plugin's issue tracker.
   //    def issueManagement = [ system: "GitHub", url: "https://github.com/grails/OmarCorePlugin/issues" ]

       // Online location of the plugin's browseable source code.
   //    def scm = [ url: "https://github.com/grails/OmarCorePlugin" ]

       Closure doWithSpring() { {->
               // TODO Implement runtime spring config (optional)
           }
       }

       void doWithDynamicMethods() {
        URL.metaClass.getParams{ ->
            HashMap result = [:]
            String queryParams = delegate.query?:""

            def pairs = queryParams?.split("&")

            pairs?.each{pair->
                def kv = pair.split("=")
                if(kv?.size()==2) result."${kv[0]}" = kv[1]
            }

            result
        }
        URL.metaClass.setParams{ p ->
            String queryParams = p.findResults { if(it.value != null) it  else null }?.join("&")?.toString()
            delegate.set(delegate.protocol, delegate.host, delegate.port, delegate.authority, delegate.userInfo,
                         delegate.path, queryParams, delegate.ref)
        }
       }

       void doWithApplicationContext() {
           // TODO Implement post initialization spring config (optional)
       }

       void onChange(Map<String, Object> event) {
           // TODO Implement code that is executed when any artefact that this plugin is
           // watching is modified and reloaded. The event contains: event.source,
           // event.application, event.manager, event.ctx, and event.plugin.
       }

       void onConfigChange(Map<String, Object> event) {
           // TODO Implement code that is executed when the project configuration changes.
           // The event is the same as for 'onChange'.
       }

       void onShutdown(Map<String, Object> event) {
           // TODO Implement code that is executed when the application shuts down (optional)
       }
}
