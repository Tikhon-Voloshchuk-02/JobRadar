import { useState, useRef, useEffect } from "react";

export default function CustomSelect({ value, onChange, options, className = "" }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    function onDown(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener("mousedown", onDown);
    return () => document.removeEventListener("mousedown", onDown);
  }, []);

  const selected = options.find(o => o.value === value);

  return (
    <div
      ref={ref}
      className={`cs ${open ? "cs--open" : ""} ${className}`}
      onClick={() => setOpen(o => !o)}
    >
      <span className="cs__label">{selected?.label}</span>
      <span className="cs__arrow">▾</span>
      {open && (
        <div className="cs__dropdown">
          {options.map(o => (
            <div
              key={o.value}
              className={`cs__option${o.value === value ? " cs__option--selected" : ""}`}
              onClick={e => { e.stopPropagation(); onChange(o.value); setOpen(false); }}
            >
              {o.label}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
