package transact.logic.commands;

import static java.util.Objects.requireNonNull;
import static transact.logic.parser.CliSyntax.PREFIX_AMOUNT;
import static transact.logic.parser.CliSyntax.PREFIX_DATE;
import static transact.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static transact.logic.parser.CliSyntax.PREFIX_STAFF;
import static transact.logic.parser.CliSyntax.PREFIX_TYPE;
import static transact.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import transact.commons.core.index.Index;
import transact.commons.util.CollectionUtil;
import transact.commons.util.ToStringBuilder;
import transact.logic.Messages;
import transact.logic.commands.exceptions.CommandException;
import transact.model.Model;
import transact.model.person.Person;
import transact.model.transaction.Transaction;
import transact.model.transaction.info.Amount;
import transact.model.transaction.info.Date;
import transact.model.transaction.info.Description;
import transact.model.transaction.info.TransactionId;
import transact.model.transaction.info.Type;
import transact.ui.MainWindow.TabWindow;




/**
 * Edits the details of an existing person in the address book.
 */
public class EditTransactionCommand extends Command {

    public static final String COMMAND_WORD = "edittransaction";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the transaction identified "
            + "by the index number used in the displayed transactions list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_TYPE + "TYPE] "
            + "[" + PREFIX_DESCRIPTION + "DESCRIPTION] "
            + "[" + PREFIX_AMOUNT + "AMOUNT] "
            + "[" + PREFIX_DATE + "DATE] "
            + "[" + PREFIX_STAFF + "STAFF]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_TYPE + "E "
            + PREFIX_AMOUNT + "10000";
    public static final String MESSAGE_EDIT_TRANSACTION_SUCCESS = "Edited Transaction: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This transaction already exists in the Transaction book.";

    private final Index index;
    private final EditTransactionDescriptor editTransactionDescriptor;

    /**
     * @param index
     *            of the person in the filtered person list to edit
     * @param editTransactionDescriptor
     *            details to edit the person with
     */
    public EditTransactionCommand(Index index, EditTransactionDescriptor editTransactionDescriptor) {
        requireNonNull(index);
        requireNonNull(editTransactionDescriptor);

        this.index = index;
        this.editTransactionDescriptor = new EditTransactionDescriptor(editTransactionDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Transaction> lastShownList = model.getFilteredTransactionList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TRANSACTION_DISPLAYED_INDEX);
        }

        Transaction transactionToEdit = lastShownList.get(index.getZeroBased());
        Transaction editedTransaction = createEditedTransaction(transactionToEdit, editTransactionDescriptor);

        if (!transactionToEdit.isSameEntry(editedTransaction) && model.hasTransaction(editedTransaction)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setTransaction(transactionToEdit, editedTransaction);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_TRANSACTION_SUCCESS, Messages.format(editedTransaction)),
                TabWindow.ADDRESSBOOK);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Transaction createEditedTransaction(Transaction transactionToEdit,
                                                  EditTransactionDescriptor editTransactionDescriptor) {
        assert transactionToEdit != null;
        Type updatedType = editTransactionDescriptor.getType().orElse(transactionToEdit.getType());
        Description updatedDescription = editTransactionDescriptor.getDescription().orElse(transactionToEdit.getDescription());
        Amount updatedAmount = editTransactionDescriptor.getAmount().orElse(transactionToEdit.getAmount());
        Date updatedDate = editTransactionDescriptor.getDate().orElse(transactionToEdit.getDate());
        Person updatedStaff = editTransactionDescriptor.getStaff().orElse(transactionToEdit.getPerson());
        return new Transaction(new TransactionId(), updatedType, updatedDescription,
                updatedAmount, updatedDate, updatedStaff);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditTransactionCommand)) {
            return false;
        }

        EditTransactionCommand otherEditTransactionCommand = (EditTransactionCommand) other;
        return index.equals(otherEditTransactionCommand.index)
                && editTransactionDescriptor.equals(otherEditTransactionCommand.editTransactionDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editTransactionDescriptor", editTransactionDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will
     * replace the
     * corresponding field value of the person.
     */
    public static class EditTransactionDescriptor {

        private Type type;
        private Description description;
        private Amount amount;
        private Date date;
        private Person staff;

        public EditTransactionDescriptor() {
        }

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditTransactionDescriptor(EditTransactionDescriptor toCopy) {
            setType(toCopy.type);
            setDescription(toCopy.description);
            setAmount(toCopy.amount);
            setDate(toCopy.date);
            setStaff(toCopy.staff);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(type, description, amount, date, staff);
        }

        // Getter and Setter for 'type'
        public Optional<Type> getType() {
            return Optional.ofNullable(type);
        }

        public void setType(Type type) {
            this.type = type;
        }

        // Getter and Setter for 'description'
        public Optional<Description> getDescription() {
            return Optional.ofNullable(description);
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        // Getter and Setter for 'amount'
        public Optional<Amount> getAmount() {
            return Optional.ofNullable(amount);
        }

        public void setAmount(Amount amount) {
            this.amount = amount;
        }

        public Optional<Date> getDate() {
            return Optional.ofNullable(date);
        }

        public void setDate(Date date) {
            this.date = date;
        }

        // Getter and Setter for 'staff'
        public Optional<Person> getStaff() {
            return Optional.ofNullable(staff);
        }

        public void setStaff(Person staff) {
            this.staff = staff;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditTransactionDescriptor)) {
                return false;
            }

            EditTransactionDescriptor otherEditPersonDescriptor = (EditTransactionDescriptor) other;
            return Objects.equals(type, otherEditPersonDescriptor.type)
                    && Objects.equals(description, otherEditPersonDescriptor.description)
                    && Objects.equals(amount, otherEditPersonDescriptor.amount)
                    && Objects.equals(date, otherEditPersonDescriptor.date)
                    && Objects.equals(staff, otherEditPersonDescriptor.staff);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("type", type)
                    .add("description", description)
                    .add("amount", amount)
                    .add("date", date)
                    .add("staff", staff)
                    .toString();
        }
    }
}
