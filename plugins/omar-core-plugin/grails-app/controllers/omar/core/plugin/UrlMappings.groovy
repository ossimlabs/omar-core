package omar.core.plugin

class UrlMappings {
    static mappings = {
        "/apidoc/$action?/$id?"(controller: "apiDoc", action: "getDocuments")
    }
}
