
import java.util.Arrays;
import java.util.Random;

 class Des {

    int[] leftShiftsNumber = new int[]{1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
    private static final byte[] staticHexCharLookup = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final char[] staticBinCharLookup = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    byte[][] key; //Stores calculated keys

    //Generates key of given length with values 0-F
    public String generateKey(int length) {
        Random rand = new Random();
        String result = "";
        for (int i = 0; i < length; i++) {
            int myRandomNumber = rand.nextInt(16); // Generates a random number between 0x10 and 0x20
            //System.out.printf("%x\n", myRandomNumber); // Prints it in hex, such as "0x14"
            result += Integer.toHexString(myRandomNumber); // Random hex number in result
        }
        return result.toUpperCase();
    }

    //Encrypt file
    public byte[] encryptFile(byte[] fileBytes, String keyStr) {
        byte[] fileBit = FileR.byteToBit2(fileBytes); //Converts byte arr to bits arr
        boolean lastBlockNotFull = false;

        int encFileBitsSize;
        if (fileBit.length % 64 > 0) {
            int blockNumber = fileBit.length / 64;
            blockNumber++;
            encFileBitsSize = blockNumber * 64;
            lastBlockNotFull = true;
        } else {
            encFileBitsSize = fileBit.length;
        }

        byte[] encryptedFileBits = new byte[encFileBitsSize];
        int iterationLength = encFileBitsSize / 64; //Every iteration loads 64 bits from input bit arr
        calculateKeys(keyStr);

        //Encrypts full 64 bits
        for (int i = 0; i < iterationLength; i++) {
            if (i == iterationLength - 1 && lastBlockNotFull) {
                int startPos = i * 64;
                int endPos = startPos + fileBit.length % 64;
                byte[] partOfNotFullInputBits = Arrays.copyOfRange(fileBit, startPos, endPos); //Copy 64 bits of fileBit arr

                byte sizeOfPaddingByteArr = (byte) ((64 - partOfNotFullInputBits.length) / 4);
                byte[] paddingBytesArr = new byte[sizeOfPaddingByteArr];
                byte numberToFillPadding = (byte) ((64 - partOfNotFullInputBits.length) / 4);
                Arrays.fill(paddingBytesArr, numberToFillPadding);
                byte[] paddingBitsArr = byteToBits(paddingBytesArr);
                byte[] fullBlock = concatArrays(partOfNotFullInputBits, paddingBitsArr);

                byte[] encryptedPartOfInput = encryptFileBits(fullBlock);
                System.arraycopy(encryptedPartOfInput, 0, encryptedFileBits, startPos, 64);
            } else {
                int startPos = i * 64;
                int endPos = (i + 1) * 64;
                byte[] partOfInputBits = Arrays.copyOfRange(fileBit, startPos, endPos); //Copy 64 bits of fileBit arr
                byte[] encryptedPartOfInput = encryptFileBits(partOfInputBits);
                System.arraycopy(encryptedPartOfInput, 0, encryptedFileBits, startPos, 64);
            }
        }

        byte[] encBitsToBytes = bitsToBytes(encryptedFileBits);
        return encBitsToBytes;
    }

    //Decrypt file
    public byte[] decryptFile(byte[] fileBytes, String keyStr) {
        byte[] fileBit = FileR.byteToBit2(fileBytes); //Converts byte arr to bits arr
        byte[] decryptedFileBits = new byte[fileBit.length];
        int iterationLength = fileBit.length / 64; //Every iteration loads 64 bits from input bit arr
        calculateKeys(keyStr);

        for (int i = 0; i < iterationLength; i++) {
            int startPos = i * 64;
            int endPos = (i + 1) * 64;
            byte[] partOfInputBits = Arrays.copyOfRange(fileBit, startPos, endPos); //Copy 64 bits of fileBit arr
            byte[] decryptedPartOfInput = decryptFileBits(partOfInputBits);
            System.arraycopy(decryptedPartOfInput, 0, decryptedFileBits, startPos, 64);
        }
        
        byte[] lastRow = Arrays.copyOfRange(decryptedFileBits, decryptedFileBits.length-64, decryptedFileBits.length);
        byte fullfillSize = checkFullfillSize(lastRow);

        byte[] encBitsToBytes;
        if(fullfillSize > 0){
            byte[] truncatedBits = Arrays.copyOf(decryptedFileBits, decryptedFileBits.length-fullfillSize*4);
            encBitsToBytes = bitsToBytes(truncatedBits);
        }else{
            encBitsToBytes = bitsToBytes(decryptedFileBits);
        }
        
        return encBitsToBytes;
    }

    // 64 input bits
    private byte checkFullfillSize(byte[] lastRowBits) {
        byte[] bytes = bitsToBytes2(lastRowBits);
        byte equalEl = 0;

        for (int i = bytes.length - 1; i > 0; i--) {
            if (bytes[i] == bytes[i - 1]) {
                equalEl++;
            } else {
                break;
            }
        }

        if (equalEl == bytes[bytes.length - 1]-1) {
            return equalEl;
        } else {
            return -1;
        }
    }

    //fileBits - 64 bits length
    public byte[] encryptFileBits(byte[] fileBits) {
        byte[] inputBlock = fileBits;
        byte[] perInput = permutInput(DesTables.IP, inputBlock);

        byte[] L = Arrays.copyOfRange(perInput, 0, 32);
        byte[] R = Arrays.copyOfRange(perInput, 32, 64);
        for (int i = 0; i < 16; i++) {
            byte[] tempR = Arrays.copyOfRange(R, 0, 32);
            byte[] currKey = key[i];
            byte[] fByte = f(R, currKey);
            byte[] lXorF = xorBitArray(L, fByte);

            //Switch parts (32 bits)
            L = tempR;
            R = lXorF;
        }
        byte[] outputBit = concatArrays(R, L);
        byte[] outputBitPerm = permutInput(DesTables.FP, outputBit);
        return outputBitPerm;
    }

    //fileBits - 64 bits length, Should calculate keys before running this method
    public byte[] decryptFileBits(byte[] fileBits) {
        byte[] inputBlock = fileBits;
        byte[] perInput = permutInput(DesTables.IP, inputBlock);

        byte[] L = Arrays.copyOfRange(perInput, 0, 32);
        byte[] R = Arrays.copyOfRange(perInput, 32, 64);
        for (int i = 15; i >= 0; i--) {
            byte[] tempR = Arrays.copyOfRange(R, 0, 32);
            byte[] currKey = key[i];
            byte[] fByte = f(R, currKey);
            byte[] lXorF = xorBitArray(L, fByte);

            //Switch parts (32 bits)
            L = tempR;
            R = lXorF;
        }
        byte[] outputBit = concatArrays(R, L);
        byte[] outputBitPerm = permutInput(DesTables.FP, outputBit);
        return outputBitPerm;
    }

    //Calculates all keys for DES for one given start key
    private void calculateKeys(String keyStr) {
        key = new byte[16][48];
        byte[] k = hexBlockToByteArray(keyStr);
        byte[] permutedKey = permutInput(DesTables.PC1, k);

        byte[] currC0 = Arrays.copyOfRange(permutedKey, 0, 28); // Left part of permuted key
        byte[] currD0 = Arrays.copyOfRange(permutedKey, 28, 56); // Right part of permuted key
        byte[] currC = currC0;
        byte[] currD = currD0;
        byte[] kPc2;
        for (int i = 0; i < 16; i++) {
            currC = shiftKey(currC, leftShiftsNumber[i]);
            currD = shiftKey(currD, leftShiftsNumber[i]);
            kPc2 = concatArrays(currC, currD);
            kPc2 = permutInput(DesTables.PC2, kPc2);
            key[i] = kPc2;
        }
    }

    //Concat arraqs into new one array. Firstly it adds from arr1, next from arr2.
    private byte[] concatArrays(byte[] arr1, byte[] arr2) {
        int arr1length = arr1.length;
        int arr2length = arr2.length;
        byte[] concArr = new byte[arr1length + arr2length];
        System.arraycopy(arr1, 0, concArr, 0, arr1length);
        System.arraycopy(arr2, 0, concArr, arr1length, arr2length);

        return concArr;
    }

    //Convert given string to the array of bytes ( bits ) For ex. 16 chars = 16 * 4 bytes ( bits) = 64 bits
    private byte[] hexBlockToByteArray(String hexBlock) {
        byte[] inputBlockBit = new byte[64];
        for (int i = 0; i < hexBlock.length(); i++) {
            byte b = hexCharToByte(hexBlock.charAt(i));
            int inputBlockBitPos = i * 4 + 3;
            for (int pos = 0; pos < 4; pos++) {
                byte singleBit = getBit(b, pos);
                inputBlockBit[inputBlockBitPos] = singleBit;
                inputBlockBitPos--;
            }

        }

        return inputBlockBit;
    }

    //left shift key
    private byte[] shiftKey(byte[] key, int shiftNumber) {
        byte b1 = key[0];
        byte b2 = key[1];

        int keyLength = key.length;
        if (shiftNumber == 2) {
            for (int i = 0; i < key.length - 2; i++) {
                key[i] = key[i + 2];
            }

            key[keyLength - 2] = b1;
            key[keyLength - 1] = b2;
        } else {
            for (int i = 0; i < key.length - 1; i++) {
                key[i] = key[i + 1];
            }

            key[keyLength - 1] = b1;
        }
        return key;
    }

    /*
    Get single bit of given byte. Right shifts given byte by pos and perform AND operation.
     */
    private byte getBit(byte inputValue, int position) {
        byte shiftedByte = (byte) (inputValue >> position);
        return (byte) (shiftedByte & 1);
    }

    /*
    Permut inputBlockBit with given permutPosition array
     */
    private byte[] permutInput(byte[] permutPosition, byte[] inputBlockBit) {
        byte[] permutedBockBit = new byte[permutPosition.length];
        for (int i = 0; i < permutPosition.length; i++) {
            byte currPos = permutPosition[i];
            permutedBockBit[i] = inputBlockBit[currPos - 1]; //currPos -1 cause pos are from 1-64 
        }
        return permutedBockBit;
    }

    private byte[] f(byte[] R, byte[] K) {
        byte[] r = permutInput(DesTables.E, R); //Extends input 32bit to 48 bit
        byte[] xorArr = xorBitArray(r, K);

        byte[] sBoxOutputBit = new byte[32];
        int currSBoxStartIndex;
        int currSBoxEndIndex;
        int sBoxOutputBitStartIndex;
        int sBoxOutputBitEndIndex;
        for (int i = 0; i < 8; i++) {
            currSBoxStartIndex = i * 6;
            currSBoxEndIndex = (i + 1) * 6;
            byte[] currSBoxInput = Arrays.copyOfRange(xorArr, currSBoxStartIndex, currSBoxEndIndex);
            byte[] singleBoxOutput = sBoxValue(currSBoxInput, i); // 4 bits

            sBoxOutputBitStartIndex = i * 4; //4 indexes - 4 bits from single sbox
            sBoxOutputBitEndIndex = (i + 1) * 4;
            setElements(sBoxOutputBit, singleBoxOutput, sBoxOutputBitStartIndex, sBoxOutputBitEndIndex);
        }
        byte[] permuttedSBoxOutputBit = permutInput(DesTables.P, sBoxOutputBit);
        return permuttedSBoxOutputBit;
    }

    //Sets elements from arrToGet to arrToSet on indexes from start(inclusive) to end(exclusive)
    private void setElements(byte[] arrToSet, byte[] arrToGet, int startPos, int endPos) {

        int currPos = startPos;
        for (int j = 0; j < 4; j++) {
            byte elToSet = arrToGet[j];
            arrToSet[currPos] = elToSet;
            currPos++;
        }

    }

    /*
    Xores every byte of given array 1 with byte on same position from array 2
     */
    private byte[] xorBitArray(byte[] arr1, byte[] arr2) {
        if (arr1.length == arr2.length) {
            byte[] xorArr = new byte[arr1.length];
            for (int i = 0; i < arr1.length; i++) {
                xorArr[i] = (byte) (arr1[i] ^ arr2[i]);
            }
            return xorArr;
        } else {
            System.out.println("Nie mozna zXORowac tablic! Nie maja takich samych rozmiarow!");
            return null;
        }
    }

    //sBoxNumber = 0..7
    //inputBits length = 6
    /*
    Calculates single sbox value for given input bits (6 bits) and sboxnumber (0-7)
     */
    private byte[] sBoxValue(byte[] inputBits, int sBoxNumber) {
        byte[] arr = new byte[4];

        int[] rowPos = {0, 5};
        int[] colPos = {1, 2, 3, 4};
        String sBoxRowStr = bitsToString(inputBits, rowPos);
        String sBoxColStr = bitsToString(inputBits, colPos);
        int sBoxRow = Integer.parseInt(sBoxRowStr, 2);
        int sBoxCol = Integer.parseInt(sBoxColStr, 2);

        byte sBoxValue = DesTables.S[sBoxNumber][sBoxRow][sBoxCol];
        int arrPos = 3;
        for (int k = 0; k < 4; k++) {
            arr[arrPos] = getBit(sBoxValue, k); //TODO sprawdzic czy zapisuje bity w dobrej kolejnosci
            arrPos--;
        }

        return arr;
    }

    //Converts input bits from positions (from array pos) to String
    private String bitsToString(byte[] input, int[] pos) {
        String convBitsStr = "";
        int currPos, currPosVal;
        for (int i = 0; i < pos.length; i++) {
            currPos = pos[i];
            currPosVal = input[currPos];
            convBitsStr += Character.toString((char) (currPosVal + 48));
        }

        return convBitsStr;
    }

    //Converts input bits to String. spaceStart indicates positions for space to appear (i*spaceStart i>0)
    private String bitsToHexString2(byte[] input, int spaceStart) {
        int maxInteration = input.length / 4;
        String str = "";
        String partStr;

        int nOfAddedChar = 0;

        for (int i = 0; i < maxInteration; i++) {
            int currPos = i * 4;
            partStr = "";
            for (int j = 0; j < 4; j++) {
                partStr += (input[currPos]);
                currPos++;
            }

            str += partStr;
            nOfAddedChar++;

        }
        if (spaceStart > 0) {
            String strWithSpace = "";
            for (int i = 0; i < str.length() / spaceStart; i++) {
                int startIndex = i * spaceStart;
                int endIndex = i * spaceStart + spaceStart;
                strWithSpace += str.substring(startIndex, endIndex);
                strWithSpace += " ";
            }
            return strWithSpace;
        } else {
            return str;
        }
    }

    //Converts input arr of bytes to String. Every byte of given array is represented as single bit. Every char of returned string is composed from 4 bytes(bits) combination.
    private String bitsToHexString3(byte[] input) {
        String hexStr = "";

        for (int i = 0; i < input.length / 4; i++) {
            byte startPos = (byte) (i * 4);
            byte b0 = input[startPos];
            byte b1 = input[startPos + 1];
            byte b2 = input[startPos + 2];
            byte b3 = input[startPos + 3];
            byte[] bitSet = new byte[]{b0, b1, b2, b3};
            String bitStr = Character.toString((char) (b0 + 48)) + Character.toString((char) (b1 + 48)) + Character.toString((char) (b2 + 48)) + Character.toString((char) (b3 + 48));
            int byteValue = Integer.parseInt(bitStr, 2);
            hexStr += intToHexChar(byteValue);
        }

        return hexStr;
    }

    //Logs input bytes to console. spaceStart indicates positions for space to appear (i*spaceStart i>0)
    private void logger(String informationalMessage, byte[] input, int spacesStart) {
        String inputString = bitsToHexString2(input, spacesStart);
        System.out.println(informationalMessage);
        System.out.println(inputString);
    }

    //Return byte value of hex char
    private byte hexCharToByte(char hexC) {
        return staticHexCharLookup[Integer.parseInt(Character.toString(hexC), 16)];
    }

    //Return char represented as given int
    private char intToHexChar(int i) {
        return staticBinCharLookup[i];
    }

    //8 bits to byte
    private byte[] bitsToBytes(byte[] bits) {
        int iterationNumber = bits.length / 8;
        byte[] bytes = new byte[iterationNumber];
        int bytesPos = 0;
        for (int i = 0; i < iterationNumber; i++) {
            byte sum = 0;

            int addPos = 0;
            for (int pos = 7; pos >= 0; pos--) {
                byte currBit = bits[i * 8 + addPos];
                sum += getByte(currBit, pos);
                addPos++;
            }
            bytes[bytesPos] = sum;
            bytesPos++;
        }

        return bytes;
    }
    
    //4 bits to byte
    private byte[] bitsToBytes2(byte[] bits) {
        int iterationNumber = bits.length / 4;
        byte[] bytes = new byte[iterationNumber];
        int bytesPos = 0;
        for (int i = 0; i < iterationNumber; i++) {
            byte sum = 0;

            int addPos = 0;
            for (int pos = 3; pos >= 0; pos--) {
                byte currBit = bits[i * 4 + addPos];
                sum += getByte(currBit, pos);
                addPos++;
            }
            bytes[bytesPos] = sum;
            bytesPos++;
        }

        return bytes;
    }

    private byte getByte(byte bit, int position) {
        byte shiftedByte = (byte) (bit << position);
        return shiftedByte;
    }

    //1 bit - 4 most right bytes, ignores 4 left bytes
    private byte[] byteToBits(byte[] byteArr) {
        byte[] bits = new byte[byteArr.length * 4];
        int bitsPos = 0;
        for (int i = 0; i < byteArr.length; i++) {
            byte currByte = byteArr[i];
            for (int pos = 3; pos >= 0; pos--) {
                byte bit = getBit(currByte, pos);
                bits[bitsPos] = bit;
                bitsPos++;
            }
        }

        return bits;
    }
}
