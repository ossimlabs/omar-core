package omar.core
import swagger.SwaggerService

class ApiController {
    def swaggerService
    def index() { }
    def toJson()
    {
        render text:swaggerService.getJsonDocument(swaggerService.scanSwaggerResources())
    }
}
