package omar.core

class CoreUrlMappings {

   static mappings = {

//     "/api/$action?/$id?"(controller: "apiDoc", action: "getDocuments")
     "/api"(controller: "apiDoc", action: "getDocuments")
     "/apiDoc/index"(controller: "apiDoc", action: "getDocuments")

      // "/api" (controller: "apis")
      // "/api/index" (controller: "apis")


   }
}
