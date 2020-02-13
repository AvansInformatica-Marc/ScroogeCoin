import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class TestIsValidTx {
    public int nPeople;
    public int nUTXOTx;
    public int maxUTXOTxOutput;
    public double maxValue;
    public int nTxPerTest;
    public int maxInput;
    public int maxOutput;
    public double pCorrupt;

    public PRGen prGen;
    public ArrayList<RSAKeyPair> people;
    public HashMap<UTXO, RSAKeyPair> utxoToKeyPair;
    public UTXOPool utxoPool;
    public ArrayList<UTXO> utxoSet;
    public int maxValidInput;

    public TxHandler txHandler;

    public TestIsValidTx() throws IOException {
        nPeople = 20;
        nUTXOTx = 20;
        maxUTXOTxOutput = 20;
        maxValue = 20;
        nTxPerTest = 50;
        maxInput = 20;
        maxOutput = 20;
        pCorrupt = 0.5;

        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) 1;
        }

        prGen = new PRGen(key);

        people = new ArrayList<>();
        for (int i = 0; i < nPeople; i++)
            people.add(new RSAKeyPair(prGen, 265));

        HashMap<Integer, RSAKeyPair> keyPairAtIndex = new HashMap<>();
        utxoToKeyPair = new HashMap<>();

        utxoPool = new UTXOPool();

        for (int i = 0; i < nUTXOTx; i++) {
            int num = SampleRandom.randomInt(maxUTXOTxOutput) + 1;
            Transaction tx = new Transaction();
            for (int j = 0; j < num; j++) {
                // pick a random public address
                int rIndex = SampleRandom.randomInt(people.size());
                RSAKey addr = people.get(rIndex).getPublicKey();
                double value = SampleRandom.randomDouble(maxValue);
                tx.addOutput(value, addr);
                keyPairAtIndex.put(j, people.get(rIndex));
            }
            tx.finalize();
            // add all tx outputs to utxo pool
            for (int j = 0; j < num; j++) {
                UTXO ut = new UTXO(tx.getHash(), j);
                utxoPool.addUTXO(ut, tx.getOutput(j));
                utxoToKeyPair.put(ut, keyPairAtIndex.get(j));
            }
        }

        utxoSet = utxoPool.getAllUTXO();
        maxValidInput = Math.min(maxInput, utxoSet.size());

        txHandler = new TxHandler(new UTXOPool(utxoPool));
    }

    @Test
    @DisplayName("Test 1: test isValidTx() with valid transactions")
    public void test1() {
        boolean passes = true;

        for (int i = 0; i < nTxPerTest; i++) {
            Transaction tx = new Transaction();
            HashMap<Integer, UTXO> utxoAtIndex = new HashMap<>();
            HashSet<UTXO> utxosSeen = new HashSet<>();
            int nInput = SampleRandom.randomInt(maxValidInput) + 1;
            double inputValue = 0;
            for (int j = 0; j < nInput; j++) {
                UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
                if (!utxosSeen.add(utxo)) {
                    j--;
                    continue;
                }
                tx.addInput(utxo.getTxHash(), utxo.getIndex());
                inputValue += utxoPool.getTxOutput(utxo).value;
                utxoAtIndex.put(j, utxo);
            }
            int nOutput = SampleRandom.randomInt(maxOutput) + 1;
            double outputValue = 0;
            for (int j = 0; j < nOutput; j++) {
                double value = SampleRandom.randomDouble(maxValue);
                if (outputValue + value > inputValue)
                    break;
                int rIndex = SampleRandom.randomInt(people.size());
                RSAKey addr = people.get(rIndex).getPublicKey();
                tx.addOutput(value, addr);
                outputValue += value;
            }
            for (int j = 0; j < nInput; j++) {
                tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
            }
            tx.finalize();
            if (!txHandler.isValidTx(tx)) {
                passes = false;
            }
        }
        Assertions.assertTrue(passes);
    }

    @Test
    @DisplayName("Test 2: test isValidTx() with transactions containing signatures of incorrect data")
    public void test2() {
        boolean passes = true;

        for (int i = 0; i < nTxPerTest; i++) {
            Transaction tx = new Transaction();
            boolean uncorrupted = true;
            HashMap<Integer, UTXO> utxoAtIndex = new HashMap<>();
            HashSet<UTXO> utxosSeen = new HashSet<>();
            int nInput = SampleRandom.randomInt(maxValidInput) + 1;
            double inputValue = 0;
            for (int j = 0; j < nInput; j++) {
                UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
                if (!utxosSeen.add(utxo)) {
                    j--;
                    continue;
                }
                tx.addInput(utxo.getTxHash(), utxo.getIndex());
                inputValue += utxoPool.getTxOutput(utxo).value;
                utxoAtIndex.put(j, utxo);
            }
            int nOutput = SampleRandom.randomInt(maxOutput) + 1;
            double outputValue = 0;
            for (int j = 0; j < nOutput; j++) {
                double value = SampleRandom.randomDouble(maxValue);
                if (outputValue + value > inputValue)
                    break;
                int rIndex = SampleRandom.randomInt(people.size());
                RSAKey addr = people.get(rIndex).getPublicKey();
                tx.addOutput(value, addr);
                outputValue += value;
            }
            for (int j = 0; j < nInput; j++) {
                byte[] rawData = tx.getRawDataToSign(j);
                if (Math.random() < pCorrupt) {
                    rawData[0]++;
                    uncorrupted = false;
                }
                tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(rawData), j);
            }
            tx.finalize();
            if (txHandler.isValidTx(tx) != uncorrupted) {
                passes = false;
            }
        }

        Assertions.assertTrue(passes);
    }

    @Test
    @DisplayName("Test 3: test isValidTx() with transactions containing signatures using incorrect private keys")
    public void test3() {
        boolean passes = true;

        for (int i = 0; i < nTxPerTest; i++) {
            Transaction tx = new Transaction();
            boolean uncorrupted = true;
            HashMap<Integer, UTXO> utxoAtIndex = new HashMap<>();
            HashSet<UTXO> utxosSeen = new HashSet<>();
            int nInput = SampleRandom.randomInt(maxValidInput - 1) + 2;
            double inputValue = 0;
            for (int j = 0; j < nInput; j++) {
                UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
                if (!utxosSeen.add(utxo)) {
                    j--;
                    continue;
                }
                tx.addInput(utxo.getTxHash(), utxo.getIndex());
                inputValue += utxoPool.getTxOutput(utxo).value;
                utxoAtIndex.put(j, utxo);
            }
            int nOutput = SampleRandom.randomInt(maxOutput) + 1;
            double outputValue = 0;
            for (int j = 0; j < nOutput; j++) {
                double value = SampleRandom.randomDouble(maxValue);
                if (outputValue + value > inputValue)
                    break;
                int rIndex = SampleRandom.randomInt(people.size());
                RSAKey addr = people.get(rIndex).getPublicKey();
                tx.addOutput(value, addr);
                outputValue += value;
            }
            for (int j = 0; j < nInput; j++) {
                RSAKeyPair keyPair = utxoToKeyPair.get(utxoAtIndex.get(j));
                if (Math.random() < pCorrupt) {
                    int index = people.indexOf(keyPair);
                    keyPair = people.get((index + 1) % nPeople);
                    uncorrupted = false;
                }
                tx.addSignature(keyPair.getPrivateKey().sign(tx.getRawDataToSign(j)), j);
            }
            tx.finalize();
            if (txHandler.isValidTx(tx) != uncorrupted) {
                passes = false;
            }
        }

        Assertions.assertTrue(passes);
    }

    @Test
    @DisplayName("Test 4: test isValidTx() with transactions whose total output value exceeds total input value")
    public void test4() {
        boolean passes = true;

        for (int i = 0; i < nTxPerTest; i++) {
            Transaction tx = new Transaction();
            boolean uncorrupted = true;
            HashMap<Integer, UTXO> utxoAtIndex = new HashMap<>();
            HashSet<UTXO> utxosSeen = new HashSet<>();
            int nInput = SampleRandom.randomInt(maxValidInput) + 1;
            double inputValue = 0;
            for (int j = 0; j < nInput; j++) {
                UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
                if (!utxosSeen.add(utxo)) {
                    j--;
                    continue;
                }
                tx.addInput(utxo.getTxHash(), utxo.getIndex());
                inputValue += utxoPool.getTxOutput(utxo).value;
                utxoAtIndex.put(j, utxo);
            }
            int nOutput = SampleRandom.randomInt(maxOutput) + 1;
            double outputValue = 0;
            for (int j = 0; j < nOutput; j++) {
                double value = SampleRandom.randomDouble(maxValue);
                if (outputValue + value > inputValue) {
                    if (Math.random() < pCorrupt) {
                        uncorrupted = false;
                    } else {
                        break;
                    }
                }
                int rIndex = SampleRandom.randomInt(people.size());
                RSAKey addr = people.get(rIndex).getPublicKey();
                tx.addOutput(value, addr);
                outputValue += value;
            }
            for (int j = 0; j < nInput; j++) {
                tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
            }
            tx.finalize();
            if (txHandler.isValidTx(tx) != uncorrupted) {
                passes = false;
            }
        }

        Assertions.assertTrue(passes);
    }

    @Test
    @DisplayName("Test 5: test isValidTx() with transactions that claim outputs not in the current utxoPool")
    public void test5() {
        boolean passes = true;

        ArrayList<RSAKeyPair> peopleExtra = new ArrayList<>();
        for (int i = 0; i < nPeople; i++)
            peopleExtra.add(new RSAKeyPair(prGen, 265));

        HashMap<Integer, RSAKeyPair> keyPairAtIndexExtra = new HashMap<>();

        UTXOPool utxoPoolExtra = new UTXOPool();

        for (int i = 0; i < nUTXOTx; i++) {
            int num = SampleRandom.randomInt(maxUTXOTxOutput) + 1;
            Transaction tx = new Transaction();
            for (int j = 0; j < num; j++) {
                // pick a random public address
                int rIndex = SampleRandom.randomInt(people.size());
                RSAKey addr = peopleExtra.get(rIndex).getPublicKey();
                double value = SampleRandom.randomDouble(maxValue);
                tx.addOutput(value, addr);
                keyPairAtIndexExtra.put(j, people.get(rIndex));
            }
            tx.finalize();
            // add all tx outputs to utxo pool
            for (int j = 0; j < num; j++) {
                UTXO ut = new UTXO(tx.getHash(), j);
                utxoPoolExtra.addUTXO(ut, tx.getOutput(j));
                utxoToKeyPair.put(ut, keyPairAtIndexExtra.get(j));
            }
        }

        ArrayList<UTXO> utxoSetExtra = utxoPoolExtra.getAllUTXO();
        int maxValidInputExtra = Math.min(maxInput, utxoSet.size() + utxoSetExtra.size());

        for (int i = 0; i < nTxPerTest; i++) {
            Transaction tx = new Transaction();
            boolean uncorrupted = true;
            HashMap<Integer, UTXO> utxoAtIndex = new HashMap<>();
            HashSet<UTXO> utxosSeen = new HashSet<>();
            int nInput = SampleRandom.randomInt(maxValidInputExtra) + 1;
            double inputValue = 0;
            for (int j = 0; j < nInput; j++) {
                if (Math.random() < pCorrupt) {
                    UTXO utxo = utxoSetExtra.get(SampleRandom.randomInt(utxoSetExtra.size()));
                    if (!utxosSeen.add(utxo)) {
                        j--;
                        continue;
                    }
                    tx.addInput(utxo.getTxHash(), utxo.getIndex());
                    inputValue += utxoPoolExtra.getTxOutput(utxo).value;
                    utxoAtIndex.put(j, utxo);
                    uncorrupted = false;
                } else {
                    UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
                    if (!utxosSeen.add(utxo)) {
                        j--;
                        continue;
                    }
                    tx.addInput(utxo.getTxHash(), utxo.getIndex());
                    inputValue += utxoPool.getTxOutput(utxo).value;
                    utxoAtIndex.put(j, utxo);
                }
            }
            int nOutput = SampleRandom.randomInt(maxOutput) + 1;
            double outputValue = 0;
            for (int j = 0; j < nOutput; j++) {
                double value = SampleRandom.randomDouble(maxValue);
                if (outputValue + value > inputValue)
                    break;
                int rIndex = SampleRandom.randomInt(people.size());
                RSAKey addr = people.get(rIndex).getPublicKey();
                tx.addOutput(value, addr);
                outputValue += value;
            }
            for (int j = 0; j < nInput; j++) {
                tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
            }
            tx.finalize();
            if (txHandler.isValidTx(tx) != uncorrupted) {
                passes = false;
            }
        }

        Assertions.assertTrue(passes);
    }

    @Test
    @DisplayName("Test 6: test isValidTx() with transactions that claim the same UTXO multiple times")
    public void test6() {
        boolean passes = true;

        for (int i = 0; i < nTxPerTest; i++) {
            Transaction tx = new Transaction();
            boolean uncorrupted = true;
            HashMap<Integer, UTXO> utxoAtIndex = new HashMap<>();
            HashSet<UTXO> utxosSeen = new HashSet<>();
            int nInput = SampleRandom.randomInt(maxValidInput) + 1;
            HashSet<UTXO> utxosToRepeat = new HashSet<>();
            int indexOfUTXOToRepeat = SampleRandom.randomInt(nInput);
            double inputValue = 0;
            for (int j = 0; j < nInput; j++) {
                UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
                if (!utxosSeen.add(utxo)) {
                    j--;
                    continue;
                }
                if (Math.random() < pCorrupt) {
                    utxosToRepeat.add(utxo);
                    uncorrupted = false;
                }
                tx.addInput(utxo.getTxHash(), utxo.getIndex());
                inputValue += utxoPool.getTxOutput(utxo).value;
                utxoAtIndex.put(j, utxo);
            }

            int count = 0;
            for (UTXO utxo : utxosToRepeat) {
                tx.addInput(utxo.getTxHash(), utxo.getIndex());
                inputValue += utxoPool.getTxOutput(utxo).value;
                utxoAtIndex.put(nInput + count, utxo);
                count++;
            }

            int nOutput = SampleRandom.randomInt(maxOutput) + 1;
            double outputValue = 0;
            for (int j = 0; j < nOutput; j++) {
                double value = SampleRandom.randomDouble(maxValue);
                if (outputValue + value > inputValue)
                    break;
                int rIndex = SampleRandom.randomInt(people.size());
                RSAKey addr = people.get(rIndex).getPublicKey();
                tx.addOutput(value, addr);
                outputValue += value;
            }
            for (int j = 0; j < (nInput + utxosToRepeat.size()); j++) {
                tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
            }
            tx.finalize();
            if (txHandler.isValidTx(tx) != uncorrupted) {
                passes = false;
            }
        }

        Assertions.assertTrue(passes);
    }

    @Test
    @DisplayName("Test 7: test isValidTx() with transactions that contain a negative output value")
    public void test7() {
        boolean passes = true;

        for (int i = 0; i < nTxPerTest; i++) {
            Transaction tx = new Transaction();
            boolean uncorrupted = true;
            HashMap<Integer, UTXO> utxoAtIndex = new HashMap<>();
            HashSet<UTXO> utxosSeen = new HashSet<>();
            int nInput = SampleRandom.randomInt(maxValidInput) + 1;
            double inputValue = 0;
            for (int j = 0; j < nInput; j++) {
                UTXO utxo = utxoSet.get(SampleRandom.randomInt(utxoSet.size()));
                if (!utxosSeen.add(utxo)) {
                    j--;
                    continue;
                }
                tx.addInput(utxo.getTxHash(), utxo.getIndex());
                inputValue += utxoPool.getTxOutput(utxo).value;
                utxoAtIndex.put(j, utxo);
            }
            int nOutput = SampleRandom.randomInt(maxOutput) + 1;
            double outputValue = 0;
            for (int j = 0; j < nOutput; j++) {
                double value = SampleRandom.randomDouble(maxValue);
                if (outputValue + value > inputValue)
                    break;
                int rIndex = SampleRandom.randomInt(people.size());
                RSAKey addr = people.get(rIndex).getPublicKey();
                if (Math.random() < pCorrupt) {
                    value = -value;
                    uncorrupted = false;
                }
                tx.addOutput(value, addr);
                outputValue += value;
            }
            for (int j = 0; j < nInput; j++) {
                tx.addSignature(utxoToKeyPair.get(utxoAtIndex.get(j)).getPrivateKey().sign(tx.getRawDataToSign(j)), j);
            }
            tx.finalize();
            if (txHandler.isValidTx(tx) != uncorrupted) {
                passes = false;
            }
        }

        Assertions.assertTrue(passes);
    }

    public static ArrayList<RSAKeyPairHelper> readKeyPairsFromFile(String filename) throws IOException {
        // Read an RSAKey from a file, return the key that was read
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        try {
            ArrayList<RSAKeyPairHelper> people = new ArrayList<>();
            int n = ois.readInt();
            for (int i = 0; i < n; i++) {
                BigInteger[] pub = (BigInteger[]) ois.readObject();
                BigInteger[] priv = (BigInteger[]) ois.readObject();
                int index = ois.readInt();
                RSAKey privKey = new RSAKey(priv[0], priv[1]);
                RSAKey pubKey = new RSAKey(pub[0], pub[1]);
                people.add(new RSAKeyPairHelper(pubKey, privKey));
            }
            ois.close();
            fis.close();
            return people;
        } catch (ClassNotFoundException x) {
            ois.close();
            fis.close();
            return null;
        }
    }
}
