import AddressFormModal from "./AddressFormModal";

function AddAddressModal({ open, onClose, onCreated }) {
  return (
    <AddressFormModal
      open={open}
      address={null}
      onClose={onClose}
      onSaved={onCreated}
    />
  );
}

export default AddAddressModal;
