package omar.core

import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class OmarWebUtils
{
	static String GZIP_ENCODE_HEADER_PARAM = 'gzip'
	
	/**
	 * Transform a string into a base64-encoded, gzipped string
	 * @param buffer The input string
	 * @return The encoded, gzipped string
	 */
	static String gzippify(String buffer /*, String charset = StandardCharsets.UTF_8.name()*/ ){
		ByteArrayOutputStream targetStream = new ByteArrayOutputStream()
		GZIPOutputStream gzipStream = new GZIPOutputStream(targetStream)
		gzipStream.write(buffer.getBytes(/*charset*/))
		gzipStream.close()

		//byte[] zippedBytes = targetStream.toByteArray()
		targetStream.close()

		//return zippedBytes.encodeBase64().toString()
		return targetStream
	}

	/**
	 * Gzippify an ArrayList by gzipping its toString
	 * @param list
	 * @return
	 */
	static String gzippify(ArrayList list, String charset = StandardCharsets.UTF_8.name()){
		return gzippify(list.toString(), charset)
	}

	static String unGzippify(String buffer, String charset = StandardCharsets.UTF_8.name()) {
		GZIPInputStream inflaterStream = new GZIPInputStream(new ByteArrayInputStream(buffer.decodeBase64()))
		String uncompressedStr = inflaterStream.getText(charset)
		return uncompressedStr
	}
}
