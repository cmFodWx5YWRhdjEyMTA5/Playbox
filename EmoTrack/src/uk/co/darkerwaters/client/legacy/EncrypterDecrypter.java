package uk.co.darkerwaters.client.legacy;

import java.util.logging.Level;

import uk.co.darkerwaters.client.EmoTrack;

import com.googlecode.gwt.crypto.bouncycastle.DataLengthException;
import com.googlecode.gwt.crypto.bouncycastle.InvalidCipherTextException;
import com.googlecode.gwt.crypto.client.TripleDesCipher;
import com.googlecode.gwt.crypto.client.TripleDesKeyGenerator;

public class EncrypterDecrypter {
	
	private byte[] GWT_DES_KEY = null;
	
	public String encryptData(String value) {
		if (null == GWT_DES_KEY) {
			GWT_DES_KEY  = new TripleDesKeyGenerator().generateNewKey();
		}

		TripleDesCipher cipher = new TripleDesCipher();
		cipher.setKey(GWT_DES_KEY);
		String enc = value;
		try {
			enc = cipher.encrypt(String.valueOf(value));
		} catch (DataLengthException e1) {
			EmoTrack.LOG.log(Level.WARNING, e1.getMessage());
		} catch (IllegalStateException e1) {
			EmoTrack.LOG.log(Level.WARNING, e1.getMessage());
		} catch (InvalidCipherTextException e1) {
			EmoTrack.LOG.log(Level.WARNING, e1.getMessage());
		}
		return enc;
	}

	public String decryptData(String enc) {
		TripleDesCipher cipher = new TripleDesCipher();
		cipher.setKey(GWT_DES_KEY);
		String dec = enc;
		try {
			dec = cipher.decrypt(enc);
		} catch (DataLengthException e) {
			EmoTrack.LOG.log(Level.WARNING, e.getMessage());
		} catch (IllegalStateException e) {
			EmoTrack.LOG.log(Level.WARNING, e.getMessage());
		} catch (InvalidCipherTextException e) {
			EmoTrack.LOG.log(Level.WARNING, e.getMessage());
		}
		return dec;
	}
}
