package omar.core

class CoreUrlMappings {

   static mappings = {

     "/api/$action?/$id?"(controller: "apiDoc", action: "getDocuments")
     "/apiDoc/index"(controller: "apiDoc", action: "getDocuments")

      "/$controller/$action?/$id?(.$format)?"{
         constraints {
            // apply constraints here
         }
      }
      // "/api" (controller: "apis")
      // "/api/index" (controller: "apis")


   }
}
