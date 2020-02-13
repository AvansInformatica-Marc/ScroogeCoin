import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestTxHandler {
    private UTXOPool uPool;

    private static String getResourcePath(final String fileName) {
        return TestTxHandler.class.getResource(fileName).getPath();
    }

    public TestTxHandler() throws IOException {
        String skpFile = getResourcePath("SampleKeyPairs.txt");
        String supFile = getResourcePath("SampleUTXOPool.txt");
        SampleKeyPairs skp = SampleKeyPairsFileHandler.readKeyPairsFromFile(skpFile);
        SampleUTXOPool sup = SampleUTXOPoolFileHandler.readSampleUTXOPoolFromFile(skp, supFile);

        uPool = sup.getPool();
    }

    // all transactions are simple and valid
    @Test
    @DisplayName("Test 1: test handleTransactions() with simple and valid transactions")
    public void test1() throws IOException {
        String common = "SampleTxsTest1-";
        String file1 = getResourcePath(common + "1.txt");
        String file2 = getResourcePath(common + "2.txt");
        String file3 = getResourcePath(common + "3.txt");
        Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
        Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
        Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

        Assertions.assertTrue(verify(allTxs1, allTxs2, allTxs3, uPool));
    }

    // all transactions are simple and valid
    @Test
    @DisplayName("Test 2: test handleTransactions() with simple but "
            + "some invalid transactions because of invalid signatures")
    public void test2() throws IOException {
        String common = "SampleTxsTest2-";
        String file1 = getResourcePath(common + "1.txt");
        String file2 = getResourcePath(common + "2.txt");
        String file3 = getResourcePath(common + "3.txt");
        Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
        Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
        Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

        Assertions.assertTrue(verify(allTxs1, allTxs2, allTxs3, uPool));
    }

    // all transactions are simple and valid
    @Test
    @DisplayName("Test 3: test handleTransactions() with simple but "
            + "some invalid transactions because of inputSum < outputSum")
    public void test3() throws IOException {
        String common = "SampleTxsTest3-";
        String file1 = getResourcePath(common + "1.txt");
        String file2 = getResourcePath(common + "2.txt");
        String file3 = getResourcePath(common + "3.txt");
        Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
        Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
        Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

        Assertions.assertTrue(verify(allTxs1, allTxs2, allTxs3, uPool));
    }

    // all transactions are simple and valid
    @Test
    @DisplayName("Test 4: test handleTransactions() with simple and "
            + "valid transactions with some double spends")
    public void test4() throws IOException {
        String common = "SampleTxsTest4-";
        String file1 = getResourcePath(common + "1.txt");
        String file2 = getResourcePath(common + "2.txt");
        String file3 = getResourcePath(common + "3.txt");
        Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
        Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
        Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

        Assertions.assertTrue(verify(allTxs1, allTxs2, allTxs3, uPool));
    }

    // all transactions are simple and valid
    @Test
    @DisplayName("Test 5: test handleTransactions() with valid but "
            + "some transactions are simple, some depend on other transactions")
    public void test5() throws IOException {
        String common = "SampleTxsTest5-";
        String file1 = getResourcePath(common + "1.txt");
        String file2 = getResourcePath(common + "2.txt");
        String file3 = getResourcePath(common + "3.txt");
        Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
        Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
        Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

        Assertions.assertTrue(verify(allTxs1, allTxs2, allTxs3, uPool));
    }

    // all transactions are simple and valid
    @Test
    @DisplayName("Test 6: test handleTransactions() with valid and simple but "
            + "some transactions take inputs from non-exisiting utxo's")
    public void test6() throws IOException {
        String common = "SampleTxsTest6-";
        String file1 = getResourcePath(common + "1.txt");
        String file2 = getResourcePath(common + "2.txt");
        String file3 = getResourcePath(common + "3.txt");
        Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
        Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
        Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

        Assertions.assertTrue(verify(allTxs1, allTxs2, allTxs3, uPool));
    }

    // all transactions are simple and valid
    @Test
    @DisplayName("Test 7: test handleTransactions() with complex Transactions")
    public void test7() throws IOException {
        String common = "SampleTxsTest7-";
        String file1 = getResourcePath(common + "1.txt");
        String file2 = getResourcePath(common + "2.txt");
        String file3 = getResourcePath(common + "3.txt");
        Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
        Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
        Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

        Assertions.assertTrue(verify(allTxs1, allTxs2, allTxs3, uPool));
    }

    // all transactions are simple and valid
    @Test
    @DisplayName("Test 8: test handleTransactions() with simple, valid transactions "
            + "being called again to check for changes made in the pool")
    public void test8() throws IOException {
        String common = "SampleTxsTest8-";
        String file1 = getResourcePath(common + "1.txt");
        String file2 = getResourcePath(common + "2.txt");
        String file3 = getResourcePath(common + "3.txt");
        Transaction[] allTxs1 = TransactionsArrayFileHandler.readTransactionsFromFile(file1);
        Transaction[] allTxs2 = TransactionsArrayFileHandler.readTransactionsFromFile(file2);
        Transaction[] allTxs3 = TransactionsArrayFileHandler.readTransactionsFromFile(file3);

        Assertions.assertTrue(verifyPoolUpdate(allTxs1, allTxs2, allTxs3, uPool));
    }

    private static boolean verify(Transaction[] allTxs1, UTXOPool uPool) {
        Transaction[] copyTxs1 = new Transaction[allTxs1.length];
        System.arraycopy(allTxs1, 0, copyTxs1, 0, copyTxs1.length);

        TxHandler student1 = new TxHandler(new UTXOPool(uPool));
        TxHandlerVerifier verifier1 = new TxHandlerVerifier(uPool);

        System.out.println("Total Transactions = " + allTxs1.length);
        Transaction[] stx1 = student1.handleTxs(copyTxs1);
        System.out.println("Number of transactions returned valid by student = " + stx1.length);

        return verifier1.check(allTxs1, stx1);
    }

    private static boolean verify(Transaction[] allTxs1, Transaction[] allTxs2, UTXOPool uPool) {
        Transaction[] copyTxs1 = new Transaction[allTxs1.length];
        System.arraycopy(allTxs1, 0, copyTxs1, 0, copyTxs1.length);

        Transaction[] copyTxs2 = new Transaction[allTxs2.length];
        System.arraycopy(allTxs2, 0, copyTxs2, 0, copyTxs2.length);

        TxHandler student1 = new TxHandler(new UTXOPool(uPool));
        TxHandlerVerifier verifier1 = new TxHandlerVerifier(uPool);

        TxHandler student2 = new TxHandler(new UTXOPool(uPool));
        TxHandlerVerifier verifier2 = new TxHandlerVerifier(uPool);

        System.out.println("Total Transactions = " + allTxs1.length);
        Transaction[] stx1 = student1.handleTxs(copyTxs1);
        System.out.println("Number of transactions returned valid by student = " + stx1.length);
        boolean passed1 = verifier1.check(allTxs1, stx1);

        System.out.println("Total Transactions = " + allTxs2.length);
        Transaction[] stx2 = student2.handleTxs(copyTxs2);
        System.out.println("Number of transactions returned valid by student = " + stx2.length);
        boolean passed2 = verifier2.check(allTxs2, stx2);

        return passed1 && passed2;
    }

    private static boolean verify(Transaction[] allTxs1, Transaction[] allTxs2, Transaction[] allTxs3, UTXOPool uPool) {
        Transaction[] copyTxs1 = new Transaction[allTxs1.length];
        System.arraycopy(allTxs1, 0, copyTxs1, 0, copyTxs1.length);

        Transaction[] copyTxs2 = new Transaction[allTxs2.length];
        System.arraycopy(allTxs2, 0, copyTxs2, 0, copyTxs2.length);

        Transaction[] copyTxs3 = new Transaction[allTxs3.length];
        System.arraycopy(allTxs3, 0, copyTxs3, 0, copyTxs3.length);

        TxHandler student1 = new TxHandler(new UTXOPool(uPool));
        TxHandlerVerifier verifier1 = new TxHandlerVerifier(uPool);

        TxHandler student2 = new TxHandler(new UTXOPool(uPool));
        TxHandlerVerifier verifier2 = new TxHandlerVerifier(uPool);

        TxHandler student3 = new TxHandler(new UTXOPool(uPool));
        TxHandlerVerifier verifier3 = new TxHandlerVerifier(uPool);

        System.out.println("Total Transactions = " + allTxs1.length);
        Transaction[] stx1 = student1.handleTxs(copyTxs1);
        System.out.println("Number of transactions returned valid by student = " + stx1.length);
        boolean passed1 = verifier1.check(allTxs1, stx1);

        System.out.println("Total Transactions = " + allTxs2.length);
        Transaction[] stx2 = student2.handleTxs(copyTxs2);
        System.out.println("Number of transactions returned valid by student = " + stx2.length);
        boolean passed2 = verifier2.check(allTxs2, stx2);

        System.out.println("Total Transactions = " + allTxs3.length);
        Transaction[] stx3 = student3.handleTxs(copyTxs3);
        System.out.println("Number of transactions returned valid by student = " + stx3.length);
        boolean passed3 = verifier3.check(allTxs3, stx3);

        return passed1 && passed2 && passed3;
    }

    private static boolean verifyPoolUpdate(Transaction[] allTxs1, Transaction[] allTxs2, Transaction[] allTxs3, UTXOPool uPool) {
        Transaction[] copyTxs1 = new Transaction[allTxs1.length];
        System.arraycopy(allTxs1, 0, copyTxs1, 0, copyTxs1.length);

        Transaction[] copyTxs2 = new Transaction[allTxs2.length];
        System.arraycopy(allTxs2, 0, copyTxs2, 0, copyTxs2.length);

        Transaction[] copyTxs3 = new Transaction[allTxs3.length];
        System.arraycopy(allTxs3, 0, copyTxs3, 0, copyTxs3.length);

        TxHandler student = new TxHandler(new UTXOPool(uPool));
        TxHandlerVerifier verifier = new TxHandlerVerifier(uPool);

        System.out.println("Total Transactions = " + allTxs1.length);
        Transaction[] stx1 = student.handleTxs(copyTxs1);
        System.out.println("Number of transactions returned valid by student = " + stx1.length);
        boolean passed1 = verifier.check(allTxs1, stx1);

        System.out.println("Total Transactions = " + allTxs2.length);
        Transaction[] stx2 = student.handleTxs(copyTxs2);
        System.out.println("Number of transactions returned valid by student = " + stx2.length);
        boolean passed2 = verifier.check(allTxs2, stx2);

        System.out.println("Total Transactions = " + allTxs3.length);
        Transaction[] stx3 = student.handleTxs(copyTxs3);
        System.out.println("Number of transactions returned valid by student = " + stx3.length);
        boolean passed3 = verifier.check(allTxs3, stx3);

        return passed1 && passed2 && passed3;
    }
}
