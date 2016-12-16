import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

/**
 * @author jmarulandah
 */
public class PinBlock {

    private static final String PIN_PAD     = "FFFFFFFFFFFFFF";
    private static final String ZERO_PAD    = "0000000000000000";
    public String               pinBlockKey = "";

    public static void main(String[] args) {
        String key = "0123456789ABCDEFFEDCBA9876543210";
        String pin = "1234";
        String pan = "7777770000075101538";

        PinBlock pbHelper = new PinBlock(key);
        String pinBlock = pbHelper.generatePinblock(pan, pin);

        if (pinBlock.equals("81C2C3AF6CA221A5")) {
            System.out.println("RESULTADO EXITOSO!");
        }

    }

    /**
     * Constructor de la clase
     * 
     * @param crytoKey Llave de cifrado de la clave.
     */
    public PinBlock(String crytoKey) {
        this.pinBlockKey = crytoKey;
    }

    /**
     * Proceso para la generación del PinBlock
     * 
     * @param pan Número de Tarjeta
     * @param pin Clave
     * @param key Llave de cifrado (32 carácteres)
     * @return Pinblock generado.
     */
    private String generatePinblock(String pan, String pin) {

        String finalPinBlock = null;
        try {
            /* Arma el PINBlock */
            byte[] pinB = this.hexStringToByteArray(this.padPin(pin));

            /* Arma el PANBlock */
            byte[] panB = this.hexStringToByteArray(this.padCard(pan));

            byte[] result = this.doXor(pinB, panB);

            finalPinBlock = pinEncryption(result);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return finalPinBlock;
    }

    /**
     * Operación Xor entre dos arreglos de Bytes
     * 
     * @param pinBlock pinBlock
     * @param panBlock panBlock
     * @return resultado de la operación XOR
     */
    private byte[] doXor(byte[] pinBlock, byte[] panBlock) {
        byte[] result = new byte[pinBlock.length];

        for (int i = 0; i < pinBlock.length; i++) {
            result[i] = (byte) (pinBlock[i] ^ panBlock[i]);
        }
        return result;
    }

    /**
     * Genera una llave triple.
     * 
     * @param key Llave de cifrado (32 carácteres)
     * @return Llave triple.
     */
    private static String getCipherKey(String key) {
        String newKey = key + key.substring(0, 16);
        return newKey;
    }

    /**
     * Proceso de cifrado de la Clave.
     * 
     * @param xorData Resultado de la operación XOR entre el Pinblock y el
     *        Panblock.
     * @param key Llave triple de cifrado
     * @return Pinblock generado.
     */
    private String pinEncryption(byte[] xorData) {
        DESedeKeySpec keyspec;
        String pinBlock = null;
        String key = getCipherKey(this.pinBlockKey);
        try {
            keyspec = new DESedeKeySpec(this.hexStringToByteArray(key));

            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
            SecretKey secretKey = keyfactory.generateSecret(keyspec);

            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedPinBlock = cipher.doFinal(xorData);
            pinBlock = getHexString(encryptedPinBlock).substring(0, 16).toUpperCase();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return pinBlock;
    }

    /**
     * Genera un bloque de 16 digitos correspondiente al PinBlock
     * 
     * @param pin Clave
     * @return PinBlock PinBlock construido
     * @throws IllegalBlockSizeException
     */
    private String padPin(String pin) throws IllegalBlockSizeException {
        String pinBlockString = "0" + pin.length() + pin + PIN_PAD;
        pinBlockString = pinBlockString.substring(0, 16);
        return pinBlockString;

    }

    /**
     * Genera un bloque de 16 digitos correspondiente al PanBlock
     * 
     * @param cardNumber Número de tarjeta
     * @return PanBlock contruído
     * @throws IllegalBlockSizeException
     */
    private String padCard(String cardNumber) throws IllegalBlockSizeException {
        cardNumber = ZERO_PAD + cardNumber;
        int cardNumberLength = cardNumber.length();
        int beginIndex = cardNumberLength - 13;
        String acctNumber = "0000"
                + cardNumber.substring(beginIndex, cardNumberLength - 1);
        return acctNumber;
    }

    /**
     * Convierte un String (Hex) en un arreglo de bytes.
     * 
     * @param hexData String de entrada con caracteres hexadecimales
     * @return Arreglo de bytes
     */
    private byte[] hexStringToByteArray(String hexData) {
        int len = hexData.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexData.charAt(i), 16) << 4) + Character.digit(
                    hexData.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Convierte un arreglo de bytes en un String con caracteres hexadecimales
     * 
     * @param inputBytes Arreglo de bytes
     * @return String con carácteres hexadecimales
     */
    private String getHexString(byte[] inputBytes) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte hexByte : inputBytes) {
            int res = 0xFF & hexByte;
            String hexString = Integer.toHexString(res);
            if (hexString.length() == 1) {
                strBuilder.append(0);
            }
            strBuilder.append(hexString);

        }

        return strBuilder.toString();
    }

}
