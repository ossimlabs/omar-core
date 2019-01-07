package omar.core
import org.springframework.beans.factory.annotation.*
import groovy.xml.StreamingMarkupBuilder


class BannersTagLib {
    static namespace = "o2"
    static defaultEncodeAs = [taglib:'raw']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

 	@Value('${securityClassification.backgroundColor}')
    def backgroundColor

    @Value('${securityClassification.classification}')
    def classification

    @Value('${securityClassification.textColor}')
    def textColor

    def securityClassificationBanner = { attrs, body ->

    	// out << "<div class='navbar navbar-default navbar-fixed-${attrs.position ?: 'top'} text-center' style='background-color: ${backgroundColor};'><p style='color: ${textColor}; font-size: 19px'>${classification}</p></div>"

    	def x = {
    		div ("class": "navbar navbar-default navbar-fixed-${attrs.position ?: 'top'} text-center classified-text",
    			 style: "background: ${backgroundColor};")
    		{

    			p (style: "margin-top: 3px; text-size: 19px; color: ${textColor};") {
    				strong(classification)
    			}
    		}
    	}


    	def text = new StreamingMarkupBuilder().bind(x).toString()
    	out << text
    }
}
