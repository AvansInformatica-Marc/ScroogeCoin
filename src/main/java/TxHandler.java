import java.util.ArrayList;
import java.util.HashSet;

public class TxHandler {
    private final UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent
     * transaction outputs) is utxoPool. This should make a defensive copy of
     * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        pool = utxoPool;
    }

    /**
     * Returns true if
     * (1) all outputs claimed by tx are in the current UTXO pool,
     * (2) the signatures on each input of tx are valid,
     * (3) no UTXO is claimed multiple times by tx,
     * (4) all of tx’s output values are non-negative, and
     * (5) the sum of tx’s input values is greater than or equal to the sum of its output values;
     * and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        final var inputs = tx.getInputs();
        var inputSum = 0.0;
        final var utxoList = new HashSet<UTXO>();

        for (final var input : inputs) {
            final var utxo = createUtxoFromTransactionInput(input);

            // (1) all outputs claimed by tx are in the current UTXO pool
            if (!pool.contains(utxo))
                return false;

            final var output = pool.getTxOutput(utxo);

            inputSum += output.value;
            // (2) the signatures on each input of tx are valid
            if (!output.address.verifySignature(tx.getRawDataToSign(inputs.indexOf(input)), input.signature))
                return false;

            // (3) no UTXO is claimed multiple times by tx
            if(utxoList.contains(utxo))
                return false;

            utxoList.add(utxo);
        }

        for (final var output : tx.getOutputs()) {
            inputSum -= output.value;
            if (output.value < 0)
            	return false;  // (4) all of tx’s output values are non-negative
        }

        // (5) the sum of tx’s input values is greater than or equal to the sum of its output values
        return inputSum >= 0;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed
     * transactions, checking each transaction for correctness,
     * returning a mutually valid array of accepted transactions,
     * and updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] proposedTransactions) {
        final var acceptedTransactions = new ArrayList<Transaction>();

        for (final var transaction : proposedTransactions) {
            if (!isValidTx(transaction))
                continue;

            acceptedTransactions.add(transaction);

            // Update the pool: remove old UTXOs and add new ones
            for (final var input : transaction.getInputs())
                this.pool.removeUTXO(createUtxoFromTransactionInput(input));

            final var txHash = transaction.getHash();
            var index = 0;
            for (final var output : transaction.getOutputs()) {
                this.pool.addUTXO(new UTXO(txHash, index), output);
                index += 1;
            }
        }

        return acceptedTransactions.toArray(new Transaction[0]);
    }

    private static UTXO createUtxoFromTransactionInput(Transaction.Input input) {
        return new UTXO(input.prevTxHash, input.outputIndex);
    }
} 
