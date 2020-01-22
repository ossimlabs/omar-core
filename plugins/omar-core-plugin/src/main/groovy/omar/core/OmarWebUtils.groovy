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
	static ByteArrayOutputStream gzippify( String buffer, String charset = StandardCharsets.UTF_8.name() ){
		ByteArrayOutputStream targetStream = new ByteArrayOutputStream()
		GZIPOutputStream gzipStream = new GZIPOutputStream(targetStream)
		gzipStream.write(buffer.getBytes( charset ))
		gzipStream.close()

		targetStream.close()

		return targetStream
	}
}
