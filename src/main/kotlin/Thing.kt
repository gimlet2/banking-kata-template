import java.time.Instant

fun main() {
    val account = Account("UsersAccount#1".name, 0L.money.balance)
    val result = account
            .deposit(123L.money.moving)
            .withdraw(100L.money.moving)
            .deposit(123L.money.moving)
    println(result.statement)
}

inline class AccountName(val value: String)

val String.name get() = AccountName(this)

class Money(val amount: Long) {
    override fun toString(): String {
        return amount.toString()
    }
}

val Long.money get() = Money(this)
val Money.balance get() = BalanceMoney(this)
val Money.moving
    get():MovingMoney {
        assert(this.amount > 0)
        return MovingMoney(this)
    }

inline class BalanceMoney(val money: Money)
inline class MovingMoney(val money: Money)

sealed class Account {
    companion object Companion

    abstract val balance: BalanceMoney
}

object EmptyAccount : Account() {
    override val balance: BalanceMoney get() = 0L.money.balance
}

data class OperationalAccount(val accountName: AccountName, override val balance: BalanceMoney, val time: RecordTime = RecordTime.now, val parent: Account = EmptyAccount) : Account()

operator fun Account.Companion.invoke(accountName: AccountName, balance: BalanceMoney) = OperationalAccount(accountName, balance, RecordTime.now)


fun OperationalAccount.deposit(amount: MovingMoney) = copy(balance = balance + amount, time = RecordTime.now, parent = this)
fun OperationalAccount.withdraw(amount: MovingMoney) = copy(balance = balance - amount, time = RecordTime.now, parent = this)


val Account.statement: String
    get() = when (this) {
        is EmptyAccount -> "---Account Statement---"
        is OperationalAccount -> parent.statement + "\n$time | ${balance - parent.balance} | $balance"
    }


operator fun BalanceMoney.plus(amount: MovingMoney) = (this.money.amount + amount.money.amount).money.balance
operator fun BalanceMoney.minus(amount: MovingMoney) = (this.money.amount - amount.money.amount).money.balance
operator fun BalanceMoney.minus(amount: BalanceMoney) = (this.money.amount - amount.money.amount).money.moving

inline class RecordTime(private val date: Instant) {
    companion object {
        val now get() = Instant.now().record
    }

    override fun toString(): String {
        return date.toString()
    }
}

val Instant.record get() = RecordTime(this)