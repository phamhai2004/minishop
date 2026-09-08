import { useRef } from "react";

export default function ImageSearchButton({ onSelect }) {
  const inputRef = useRef();

  const openFile = () => {
    inputRef.current.click();
  };

  const chooseImage = (e) => {
    const file = e.target.files[0];

    if (file) {
      onSelect(file);
    }
  };

  return (
    <>
      <button type="button" onClick={openFile}>
        📷
      </button>

      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        hidden
        onChange={chooseImage}
      />
    </>
  );
}
