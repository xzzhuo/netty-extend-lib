/**
 * Author: xiaozhao
 */

package exhi.net.interface1;

public enum NetCharset {

	/**
	 * Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
	 */
	US_ASCII,
	
	/**
	 * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
	 */
	ISO_8859_1,
	
	/**
	 * Eight-bit UCS Transformation Format
	 */
	UTF_8,
	
	/**
	 * Sixteen-bit UCS Transformation Format, big-endian byte order
	 */
	UTF_16BE,
	
	/**
	 * Sixteen-bit UCS Transformation Format, little-endian byte order
	 */
	UTF_16LE,
	
	/**
	 * Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
	 */
	UTF_16,
	
}
