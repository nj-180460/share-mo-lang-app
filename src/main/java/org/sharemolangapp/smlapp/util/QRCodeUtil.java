package org.sharemolangapp.smlapp.util;

import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;



public abstract class QRCodeUtil {
	
	private QRCodeUtil() {
		throw new UnsupportedOperationException(getClass().getName() + " is unavailable. Use static methods only.");
	}
	
	
	// generate qr code without logo or text
	private static BufferedImage qrCodeGenerator(String qrCodeContent) throws WriterException {
		// Create new configuration that specifies the error correction
		Map<EncodeHintType, ErrorCorrectionLevel> hints = new LinkedHashMap<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
	    QRCodeWriter barcodeWriter = new QRCodeWriter();
	    BitMatrix bitMatrix = barcodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, 400, 400, hints);
		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}
	
	
	
//	private static BufferedImage addLogoWithText(Path resourcePathFile, BufferedImage bufferedQRCodeImage, String qrcodeTextBelow) 
//			throws IOException {
//		
//	    // Initialize combined image
//	    BufferedImage combinedLogoWithText = new BufferedImage(
//	    		bufferedQRCodeImage.getHeight(), bufferedQRCodeImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
//	    Graphics2D g = (Graphics2D) combinedLogoWithText.getGraphics();
//	    
//	    // Calculate the delta height and width between QR code and logo
//		int qrcodeHeight = bufferedQRCodeImage.getHeight();
//	    int qrcodeWidth = bufferedQRCodeImage.getWidth();
//	    
//	    // Write QR code to new image at position 0/0
//	    g.drawImage(bufferedQRCodeImage, 0, 0, null);
//	    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
//	    
//	    // adding text
//	    AttributedString attributedText = new AttributedString(qrcodeTextBelow);
////	    attributedText.addAttribute(TextAttribute.FONT, Fonts.TIMEDATE_FONT_SEGOE);
//	    attributedText.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
//	    g.drawString(attributedText.getIterator(), (int) Math.round(qrcodeWidth*0.30), (int) Math.round(qrcodeHeight*0.98));
//	    // logo
//	    if(resourcePathFile != null) {
//		    // initialize buffered image for logo
//	    	BufferedImage logoImage = ImageIO.read(resourcePathFile.toFile());
//		    int deltaHeight = qrcodeHeight - logoImage.getHeight();
//		    int deltaWidth = qrcodeWidth - logoImage.getWidth();
//		    
//		    // Write logo into combine image at position (deltaWidth / 2) and
//		    // (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
//		    // the same space for the logo to be centered
//		    g.drawImage(logoImage, (int) Math.round(deltaWidth * 0.50), (int) Math.round(deltaHeight * 0.50), null);
//	    }
//	    
//	    // Write combined image as PNG to OutputStream - saves to local
//	    StringBuilder filenameSB = new StringBuilder();
//	    filenameSB.append(ConfigConstant.getQRCodeDirFullPath());
//	    filenameSB.append(qrcodeTextBelow);
//	    filenameSB.append(ConfigConstant.PNG_EXTENSION);
//	    try(FileOutputStream outputQRCodeImageFile = new FileOutputStream(Paths.get(filenameSB.toString()).toFile())){
//	    	ImageIO.write(combinedLogoWithText, ConfigConstant.PNG_EXTENSION.replace(".","").strip(), outputQRCodeImageFile);
//	    }
//	    
//		return combinedLogoWithText;
//	}
	
	/**
	 * Generate QR Code image with a name and a logo in it.
	 * @param qrcodeContent - encrypted text which will be used to generate qr code
	 * @param withLogo - boolean which will used to filter if the image will have logo or not. True with logo in image, otherwise no logo
	 * @param vaccineeProfile - vaccinee, formatted in order from first name, middle name, last name, and suffix
	 * @return BufferedImage object
	 * @throws URISyntaxException 
	 * @throws WriterException 
	 * */
	public static BufferedImage generateBufferedQRCodeImage(Properties serverProp)
			throws URISyntaxException, WriterException {
		
		StringBuilder qrcodeContent = new StringBuilder();
		qrcodeContent.append(serverProp.getProperty("host")).append(":");
		qrcodeContent.append(serverProp.getProperty("port"));
		
	    BufferedImage bufferedQRCode = qrCodeGenerator(qrcodeContent.toString());
//	    
//	    // add Laoang Logo
//	    StringBuilder textNameToImprint = new StringBuilder();
//	    textNameToImprint.append(serverProp.getProperty("host")).append(":");
//	    textNameToImprint.append(serverProp.getProperty("port"));
//	    try {
//	    	return addLogoWithText(
//	    			withLogo ? resourceLogoPathFile : null,
//	    			bufferedQRCode,
//	    			textNameToImprint.toString().toUpperCase());
//	    } catch(IOException ioex) {
//	    	ioex.printStackTrace();
//	    	// Failed adding logo and text. DO nothing
//	    }
	    return bufferedQRCode;
	}
	
	
	public static String readQRCodeImage(BufferedImage image) {
		QRCodeMultiReader qrcodeReader = new QRCodeMultiReader();
		Result result = null;
		if (image != null) {
			LuminanceSource source = new BufferedImageLuminanceSource(image);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			try {
				result = qrcodeReader.decode(bitmap);
			} catch (NotFoundException | ChecksumException | FormatException e) {
				// No QR Code in image
				// do nothing. allow it to throw exception la
			}
		}
		return result.getText();
	}
	
}
