package omar.core
import org.springframework.beans.factory.annotation.*
import groovy.xml.StreamingMarkupBuilder


class BannersTagLib {
    static namespace = "o2"
    static defaultEncodeAs = [taglib:'raw']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

 	@Value('${securityClassificationBanner.backgroundColor}')
    def backgroundColor

    @Value('${securityClassificationBanner.classification}')
    def classification

    @Value('${securityClassificationBanner.textColor}')
    def textColor

    def securityClassificationBanner = { attrs, body ->

    	// out << "<div class='navbar navbar-default navbar-fixed-${attrs.position ?: 'top'} text-center' style='background-color: ${backgroundColor};'><p style='color: ${textColor}; font-size: 19px'>${classification}</p></div>"

    	def x = {
    		div ("class": "navbar navbar-default navbar-fixed-${attrs.position ?: 'top'} text-center",
    			 style: "background: ${backgroundColor};")
    		{

    			p (style: "margin-top: 3px; text-size: 19px; color: ${textColor}; text-shadow: 0px .5px .5px #fff;") {
    				strong(classification)
    			}
    		}
    	}


    	def text = new StreamingMarkupBuilder().bind(x).toString()
    	out << text
    }
}
