package org.bitcoindevkit

/**
 * The cryptocurrency to act on.
 *
 * @sample org.bitcoindevkit.networkSample
 */
enum class Network {
    /** Bitcoin's mainnet. */
    BITCOIN,

    /** Bitcoin’s testnet. */
    TESTNET,

    /** Bitcoin’s signet. */
    SIGNET,

    /** Bitcoin’s regtest. */
    REGTEST,
}

/**
 * A derived address and the index it was found at.
 *
 * @property index Child index of this address.
 * @property address Address.
 *
 * @sample org.bitcoindevkit.addressInfoSample
 */
data class AddressInfo (
    var index: UInt,
    var address: String
)

/**
 * The address index selection strategy to use to derive an address from the wallet’s external descriptor.
 *
 * If you’re unsure which one to use, use `AddressIndex.NEW`.
 *
 * @sample org.bitcoindevkit.addressIndexSample
 */
enum class AddressIndex {
    /** Return a new address after incrementing the current descriptor index. */
    NEW,

    /** Return the address for the current descriptor index if it has not been used in a received transaction.
     * Otherwise return a new address as with `AddressIndex.NEW`. Use with caution, if the wallet
     * has not yet detected an address has been used it could return an already used address.
     * This function is primarily meant for situations where the caller is untrusted;
     * for example when deriving donation addresses on-demand for a public web page.
     */
    LAST_UNUSED,
}

/**
 * Balance differentiated in various categories.
 *
 * @property immature All coinbase outputs not yet matured.
 * @property trustedPending Unconfirmed UTXOs generated by a wallet tx.
 * @property untrustedPending Unconfirmed UTXOs received from an external wallet.
 * @property confirmed Confirmed and immediately spendable balance.
 * @property spendable The sum of trustedPending and confirmed coins.
 * @property total The whole balance visible to the wallet.
 *
 * @sample org.bitcoindevkit.balanceSample
 */
data class Balance (
    var immature: ULong,
    var trustedPending: ULong,
    var untrustedPending: ULong,
    var confirmed: ULong,
    var spendable: ULong,
    var total: ULong
)

/**
 * Type that can contain any of the database configurations defined by the library.
 *
 * @sample org.bitcoindevkit.memoryDatabaseConfigSample
 * @sample org.bitcoindevkit.sqliteDatabaseConfigSample
 */
sealed class DatabaseConfig {
    /** Configuration for an in-memory database. */
    object Memory : DatabaseConfig()

    /** Configuration for a Sled database. */
    data class Sled(val config: SledDbConfiguration) : DatabaseConfig()

    /** Configuration for a SQLite database. */
    data class Sqlite(val config: SqliteDbConfiguration) : DatabaseConfig()
}

/**
 * Configuration type for a SQLite database.
 *
 * @property path Main directory of the DB.
 *
 * @sample org.bitcoindevkit.sqliteDatabaseConfigSample
 */
data class SqliteDbConfiguration(
    var path: String,
)

/**
 * Configuration type for a SledDB database.
 *
 * @property path Main directory of the DB.
 * @property treeName Name of the database tree, a separated namespace for the data.
 */
data class SledDbConfiguration(
    var path: String,
    var treeName: String,
)

/**
 * Configuration for an Electrum blockchain.
 *
 * @property url URL of the Electrum server (such as ElectrumX, Esplora, BWT) may start with `ssl://` or `tcp://` and include a port, e.g. `ssl://electrum.blockstream.info:60002`.
 * @property socks5 URL of the socks5 proxy server or a Tor service.
 * @property retry Request retry count.
 * @property timeout Request timeout (seconds).
 * @property stopGap Stop searching addresses for transactions after finding an unused gap of this length.
 *
 * @sample org.bitcoindevkit.electrumBlockchainConfigSample
 */
data class ElectrumConfig(
    var url: String,
    var socks5: String?,
    var retry: UByte,
    var timeout: UByte?,
    var stopGap: ULong
)

/**
 * Configuration for an Esplora blockchain.
 *
 * @property baseUrl Base URL of the esplora service, e.g. `https://blockstream.info/api/`.
 * @property proxy Optional URL of the proxy to use to make requests to the Esplora server.
 * @property concurrency Number of parallel requests sent to the esplora service (default: 4).
 * @property stopGap Stop searching addresses for transactions after finding an unused gap of this length.
 * @property timeout Socket timeout.
 *
 * @sample org.bitcoindevkit.esploraBlockchainConfigSample
 */
data class EsploraConfig(
    var baseUrl: String,
    var proxy: String?,
    var concurrency: UByte?,
    var stopGap: ULong,
    var timeout: ULong?
)

/**
 * Authentication mechanism for RPC connection to full node
 */
sealed class Auth {
    /** No authentication */
    object None: Auth()

    /** Authentication with username and password, usually [Auth.Cookie] should be preferred */
    data class UserPass(val username: String, val password: String): Auth()

    /** Authentication with a cookie file */
    data class Cookie(val file: String): Auth()
}

/**
 * Sync parameters for Bitcoin Core RPC.
 *
 * In general, BDK tries to sync `scriptPubKey`s cached in `Database` with
 * `scriptPubKey`s imported in the Bitcoin Core Wallet. These parameters are used for determining
 * how the `importdescriptors` RPC calls are to be made.
 *
 * @property startScriptCount The minimum number of scripts to scan for on initial sync.
 * @property startTime Time in unix seconds in which initial sync will start scanning from (0 to start from genesis).
 * @property forceStartTime Forces every sync to use `start_time` as import timestamp.
 * @property pollRateSec RPC poll rate (in seconds) to get state updates.
 */
data class RcpSyncParams(
    val startScriptCount: ULong,
    val startTime: Ulong,
    val forceStartTime: Boolean,
    val pollRateSec: ULong,
)

/**
 * RpcBlockchain configuration options
 *
 * @property url The bitcoin node url.
 * @property auth The bicoin node authentication mechanism.
 * @property network The network we are using (it will be checked the bitcoin node network matches this).
 * @property walletName The wallet name in the bitcoin node.
 * @property syncParams Sync parameters.
 */
data class RpcConfig(
    val url: String,
    val auth: Auth,
    val network: Network,
    val walletName: String,
    val syncParams: RcpSyncParams?,
)

/**
 * Type that can contain any of the blockchain configurations defined by the library.
 *
 * @sample org.bitcoindevkit.electrumBlockchainConfigSample
 */
sealed class BlockchainConfig {
    /** Electrum client. */
    data class Electrum(val config: ElectrumConfig) : BlockchainConfig()

    /** Esplora client. */
    data class Esplora(val config: EsploraConfig) : BlockchainConfig()

    /** Bitcoin Core RPC client. */
    data class Rpc(val config: RpcConfig) : BlockchainConfig()
}

/**
 * A wallet transaction.
 *
 * @property fee Fee value (sats) if available. The availability of the fee depends on the backend. It’s never None with an Electrum server backend, but it could be None with a Bitcoin RPC node without txindex that receive funds while offline.
 * @property received Received value (sats) Sum of owned outputs of this transaction.
 * @property sent Sent value (sats) Sum of owned inputs of this transaction.
 * @property txid Transaction id.
 * @property confirmationTime If the transaction is confirmed, [BlockTime] contains height and timestamp of the block containing the transaction. This property is null for unconfirmed transactions.
 */
data class TransactionDetails (
    var fee: ULong?,
    var received: ULong,
    var sent: ULong,
    var txid: String,
    var confirmationTime: BlockTime?
)

/**
 * A blockchain backend.
 *
 * @constructor Create the new blockchain client.
 *
 * @param config The blockchain configuration required.
 *
 * @sample org.bitcoindevkit.blockchainSample
 */
class Blockchain(
    config: BlockchainConfig
) {
    /** Broadcast a transaction. */
    fun broadcast(psbt: PartiallySignedBitcoinTransaction) {}

    /** Get the current height of the blockchain. */
    fun getHeight(): UInt {}

    /** Get the block hash of a given block. */
    fun getBlockHash(height: UInt): String {}
}

/**
 * A partially signed bitcoin transaction.
 *
 * @constructor Build a new Partially Signed Bitcoin Transaction.
 *
 * @param psbtBase64 The PSBT in base64 format.
 */
class PartiallySignedBitcoinTransaction(psbtBase64: String) {
    /** Return the PSBT in string format, using a base64 encoding. */
    fun serialize(): String {}

    /** Get the txid of the PSBT. */
    fun txid(): String {}

    /** Return the transaction as bytes. */
    fun extractTx(): List<UByte>

    /**
     * Combines this PartiallySignedTransaction with another PSBT as described by BIP 174.
     * In accordance with BIP 174 this function is commutative i.e., `A.combine(B) == B.combine(A)`
     */
    fun combine(other: PartiallySignedBitcoinTransaction): PartiallySignedBitcoinTransaction
}

/**
 * A reference to a transaction output.
 *
 * @property txid The referenced transaction’s txid.
 * @property vout The index of the referenced output in its transaction’s vout.
 */
data class OutPoint (
    var txid: String,
    var vout: UInt
)

/**
 * A transaction output, which defines new coins to be created from old ones.
 *
 * @property value The value of the output, in satoshis.
 * @property address The address of the output.
 */
data class TxOut (
    var value: ULong,
    var address: String
)

/**
 * An unspent output owned by a [Wallet].
 *
 * @property outpoint Reference to a transaction output.
 * @property txout Transaction output.
 * @property keychain Type of keychain.
 * @property isSpent Whether this UTXO is spent or not.
 */
data class LocalUtxo (
    var outpoint: OutPoint,
    var txout: TxOut,
    var keychain: KeychainKind,
    var isSpent: Boolean
)

/**
 * Types of keychains.
 */
enum class KeychainKind {
    /** External. */
    EXTERNAL,

    /** Internal, usually used for change outputs. */
    INTERNAL,
}

/**
 * Block height and timestamp of a block.
 *
 * @property height Confirmation block height.
 * @property timestamp Confirmation block timestamp.
 */
data class BlockTime (
    var height: UInt,
    var timestamp: ULong,
)

/**
 * A Bitcoin wallet.
 * The Wallet acts as a way of coherently interfacing with output descriptors and related transactions. Its main components are:
 * 1. Output descriptors from which it can derive addresses.
 * 2. A Database where it tracks transactions and utxos related to the descriptors.
 * 3. Signers that can contribute signatures to addresses instantiated from the descriptors.
 *
 * @constructor Create a BDK wallet.
 *
 * @param descriptor The main (or "external") descriptor.
 * @param changeDescriptor The change (or "internal") descriptor.
 * @param network The network to act on.
 * @param databaseConfig The database configuration.
 *
 * @sample org.bitcoindevkit.walletSample
 */
class Wallet(
    descriptor: String,
    changeDescriptor: String,
    network: Network,
    databaseConfig: DatabaseConfig,
) {
    /**
     * Return a derived address using the external descriptor, see [AddressIndex] for available address index
     * selection strategies. If none of the keys in the descriptor are derivable (i.e. the descriptor does not end
     * with a * character) then the same address will always be returned for any [AddressIndex].
     */
    fun getAddress(addressIndex: AddressIndex): AddressInfo {}

    /** Return the wallet's balance, across different categories. See [Balance] for the categories. Note that this method only operates on the internal database, which first needs to be [Wallet.sync] manually. */
    fun getBalance(): Balance {}

    /** Sign a transaction with all the wallet’s signers. */
    fun sign(psbt: PartiallySignedBitcoinTransaction): Boolean {}

    /** Return the list of transactions made and received by the wallet. Note that this method only operate on the internal database, which first needs to be [Wallet.sync] manually. */
    fun listTransactions(): List<TransactionDetails> {}

    /** Get the Bitcoin network the wallet is using. */
    fun network(): Network {}

    /** Sync the internal database with the blockchain. */
    fun sync(blockchain: Blockchain, progress: Progress?) {}

    /** Return the list of unspent outputs of this wallet. Note that this method only operates on the internal database, which first needs to be [Wallet.sync] manually. */
    fun listUnspent(): List<LocalUtxo> {}
}

/**
 * Class that logs at level INFO every update received (if any).
 */
class Progress {
    /** Send a new progress update. The progress value should be in the range 0.0 - 100.0, and the message value is an optional text message that can be displayed to the user. */
    fun update(progress: Float, message: String?) {}
}

/**
 * A transaction builder.
 *
 * After creating the TxBuilder, you set options on it until finally calling `.finish` to consume the builder and generate the transaction.
 *
 * Each method on the TxBuilder returns an instance of a new TxBuilder with the option set/added.
 *
 * @sample org.bitcoindevkit.txBuilderResultSample1
 * @sample org.bitcoindevkit.txBuilderResultSample2
 */
class TxBuilder() {
    /** Add data as an output using OP_RETURN. */
    fun addData(data: List<UByte>): TxBuilder {}

    /** Add a recipient to the internal list. */
    fun addRecipient(script: Script, amount: ULong): TxBuilder {}

    /** Set the list of recipients by providing a list of [ScriptAmount]. */
    fun setRecipients(recipients: List<ScriptAmount>): TxBuilder {}

    /** Add a utxo to the internal list of unspendable utxos. It’s important to note that the "must-be-spent" utxos added with [TxBuilder.addUtxo] have priority over this. See the Rust docs of the two linked methods for more details. */
    fun addUnspendable(unspendable: OutPoint): TxBuilder {}

    /** Add an outpoint to the internal list of UTXOs that must be spent. These have priority over the "unspendable" utxos, meaning that if a utxo is present both in the "utxos" and the "unspendable" list, it will be spent. */
    fun addUtxo(outpoint: OutPoint): TxBuilder {}

    /**
     * Add the list of outpoints to the internal list of UTXOs that must be spent. If an error
     * occurs while adding any of the UTXOs then none of them are added and the error is returned.
     * These have priority over the "unspendable" utxos, meaning that if a utxo is present both
     * in the "utxos" and the "unspendable" list, it will be spent.
     */
    fun addUtxos(outpoints: List<OutPoint>): TxBuilder {}

    /** Do not spend change outputs. This effectively adds all the change outputs to the "unspendable" list. See [TxBuilder.unspendable]. */
    fun doNotSpendChange(): TxBuilder {}

    /** Only spend utxos added by [add_utxo]. The wallet will not add additional utxos to the transaction even if they are needed to make the transaction valid. */
    fun manuallySelectedOnly(): TxBuilder {}

    /** Only spend change outputs. This effectively adds all the non-change outputs to the "unspendable" list. See [TxBuilder.unspendable]. */
    fun onlySpendChange(): TxBuilder {}

    /**
     * Replace the internal list of unspendable utxos with a new list. It’s important to note that the "must-be-spent" utxos
     * added with [TxBuilder.addUtxo] have priority over these. See the Rust docs of the two linked methods for more details.
     */
    fun unspendable(unspendable: List<OutPoint>): TxBuilder {}

    /** Set a custom fee rate. */
    fun feeRate(satPerVbyte: Float): TxBuilder {}

    /** Set an absolute fee. */
    fun feeAbsolute(feeAmount: ULong): TxBuilder {}

    /** Spend all the available inputs. This respects filters like [TxBuilder.unspendable] and the change policy. */
    fun drainWallet(): TxBuilder {}

    /**
     * Sets the address to drain excess coins to. Usually, when there are excess coins they are
     * sent to a change address generated by the wallet. This option replaces the usual change address
     * with an arbitrary ScriptPubKey of your choosing. Just as with a change output, if the
     * drain output is not needed (the excess coins are too small) it will not be included in the resulting
     * transaction. The only difference is that it is valid to use [drainTo] without setting any ordinary recipients
     * with [addRecipient] (but it is perfectly fine to add recipients as well). If you choose not to set any
     * recipients, you should either provide the utxos that the transaction should spend via [addUtxos], or set
     * [drainWallet] to spend all of them. When bumping the fees of a transaction made with this option,
     * you probably want to use [BumpFeeTxBuilder.allowShrinking] to allow this output to be reduced to pay for the extra fees.
     */
    fun drainTo(script: Script): TxBuilder {}

    /** Enable signaling RBF. This will use the default `nsequence` value of `0xFFFFFFFD`. */
    fun enableRbf(): TxBuilder {}

    /**
     * Enable signaling RBF with a specific nSequence value. This can cause conflicts if the wallet's descriptors
     * contain an "older" (OP_CSV) operator and the given `nsequence` is lower than the CSV value. If the `nsequence`
     * is higher than `0xFFFFFFFD` an error will be thrown, since it would not be a valid nSequence to signal RBF.
     */
    fun enableRbfWithSequence(nsequence: UInt): TxBuilder {}

    /** Finish building the transaction. Returns a [TxBuilderResult]. */
    fun finish(wallet: Wallet): TxBuilderResult {}
}

/**
 * A object holding a ScriptPubKey and an amount.
 *
 * @property script The ScriptPubKey.
 * @property amount The amount.
 */
data class ScriptAmount (
    var script: Script,
    var amount: ULong
)

/**
 * The BumpFeeTxBuilder is used to bump the fee on a transaction that has been broadcast and has its RBF flag set to true.
 */
class BumpFeeTxBuilder() {
    /**
     * Explicitly tells the wallet that it is allowed to reduce the amount of the output matching this scriptPubKey
     * in order to bump the transaction fee. Without specifying this the wallet will attempt to find a change output
     * to shrink instead. Note that the output may shrink to below the dust limit and therefore be removed. If it is
     * preserved then it is currently not guaranteed to be in the same position as it was originally. Returns an error
     * if scriptPubkey can’t be found among the recipients of the transaction we are bumping.
     */
    fun allowShrinking(address: String): BumpFeeTxBuilder {}

    /** Enable signaling RBF. This will use the default `nsequence` value of `0xFFFFFFFD`. */
    fun enableRbf(): BumpFeeTxBuilder {}

    /**
     * Enable signaling RBF with a specific nSequence value. This can cause conflicts if the wallet's descriptors
     * contain an "older" (OP_CSV) operator and the given `nsequence` is lower than the CSV value. If the `nsequence`
     * is higher than `0xFFFFFFFD` an error will be thrown, since it would not be a valid nSequence to signal RBF.
     */
    fun enableRbfWithSequence(nsequence: UInt): BumpFeeTxBuilder {}

    /** Finish building the transaction. Returns a [TxBuilderResult]. */
    fun finish(wallet: Wallet): TxBuilderResult {}
}

/**
 * A BIP-32 derivation path.
 *
 * @param path The derivation path. Must start with `m`. Use this type to derive or extend a [DescriptorSecretKey]
 * or [DescriptorPublicKey].
 */
class DerivationPath(path: String) {}

/**
 * An extended secret key.
 *
 * @param network The network this DescriptorSecretKey is to be used on.
 * @param mnemonic The mnemonic.
 * @param password The optional passphrase that can be provided as per BIP-39.
 *
 * @sample org.bitcoindevkit.descriptorSecretKeyDeriveSample
 * @sample org.bitcoindevkit.descriptorSecretKeyExtendSample
 */
class DescriptorSecretKey(network: Network, mnemonic: Mnemonic, password: String?) {
    /** Build a DescriptorSecretKey from a String */
    fun fromString(secretKey: String): DescriptorSecretKey {}

    /** Derive a private descriptor at a given path. */
    fun derive(path: DerivationPath): DescriptorSecretKey {}

    /** Extend the private descriptor with a custom path. */
    fun extend(path: DerivationPath): DescriptorSecretKey {}

    /** Return the public version of the descriptor. */
    fun asPublic(): DescriptorPublicKey {}

    /* Return the raw private key as bytes. */
    fun secretBytes(): List<UByte>

    /** Return the private descriptor as a string. */
    fun asString(): String {}
}

/**
 * An extended public key.
 *
 * @param network The network this DescriptorPublicKey is to be used on.
 * @param mnemonic The mnemonic.
 * @param password The optional passphrase that can be provided as per BIP-39.
 */
class DescriptorPublicKey(network: Network, mnemonic: String, password: String?) {
    /** Build a DescriptorPublicKey from a String */
    fun fromString(publicKey: String): DescriptorPublicKey {}

    /** Derive a public descriptor at a given path. */
    fun derive(path: DerivationPath): DescriptorPublicKey

    /** Extend the public descriptor with a custom path. */
    fun extend(path: DerivationPath): DescriptorPublicKey

    /** Return the public descriptor as a string. */
    fun asString(): String
}

/**
 * An enum describing entropy length (aka word count) in the mnemonic.
 */
enum class WordCount {
    /** 12 words mnemonic (128 bits entropy). */
    WORDS12,

    /** 15 words mnemonic (160 bits entropy). */
    WORDS15,

    /** 18 words mnemonic (192 bits entropy). */
    WORDS18,

    /** 21 words mnemonic (224 bits entropy). */
    WORDS21,

    /** 24 words mnemonic (256 bits entropy). */
    WORDS24,
}

/**
 * The value returned from calling the `.finish()` method on the [TxBuilder] or [BumpFeeTxBuilder].
 *
 * @property psbt The PSBT
 * @property transactionDetails The transaction details.
 *
 * @sample org.bitcoindevkit.txBuilderResultSample1
 * @sample org.bitcoindevkit.txBuilderResultSample2
 */
data class TxBuilderResult (
    var psbt: PartiallySignedBitcoinTransaction,
    var transactionDetails: TransactionDetails
)

/**
 * A bitcoin script.
 */
class Script(rawOutputScript: List<UByte>)

/**
 * A bitcoin address.
 *
 * @param address The address in string format.
 */
class Address(address: String) {
    /* Return the ScriptPubKey. */
    fun scriptPubkey(): Script
}

/**
 * Mnemonic phrases are a human-readable version of the private keys. Supported number of words are 12, 15, 18, 21 and 24.
 *
 * @constructor Generates Mnemonic with a random entropy.
 * @param mnemonic The mnemonic as a string of space-separated words.
 *
 * @sample org.bitcoindevkit.mnemonicSample
 */
class Mnemonic(mnemonic: String) {
    /* Returns Mnemonic as string */
    fun asString(): String

    /* Parse a Mnemonic from a given string. */
    fun fromString(): Mnemonic

    /*
     * Create a new Mnemonic in the specified language from the given entropy. Entropy must be a
     * multiple of 32 bits (4 bytes) and 128-256 bits in length.
     */
    fun fromEntropy(): Mnemonic
}
