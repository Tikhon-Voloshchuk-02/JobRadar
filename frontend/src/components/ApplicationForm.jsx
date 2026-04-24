export default function ApplicationForm({
  formData,
  onChange,
  onSubmit,
  isEditing,
}) {
  return (
    <form className="application-form" onSubmit={onSubmit}>
      <input
        type="text"
        name="company"
        placeholder="Company"
        value={formData.company}
        onChange={onChange}
        required
      />

      <input
        type="text"
        name="position"
        placeholder="Position"
        value={formData.position}
        onChange={onChange}
        required
      />

      <input
        type="url"
        name="link"
        placeholder="Vacancy link"
        value={formData.link}
        onChange={onChange}
      />

      <select
        name="status"
        value={formData.status}
        onChange={onChange}
      >
        <option value="SAVED">SAVED</option>
        <option value="APPLIED">APPLIED</option>
        <option value="WAITING">WAITING</option>
        <option value="INTERVIEW">INTERVIEW</option>
        <option value="REJECTED">REJECTED</option>
        <option value="OFFER">OFFER</option>
      </select>

      <textarea
        name="notes"
        placeholder="Notes"
        value={formData.notes}
        onChange={onChange}
      />

      <input
        type="date"
        name="appliedAt"
        value={formData.appliedAt}
        onChange={onChange}
      />

      <button type="submit">
        {isEditing ? "Update application" : "Save application"}
      </button>
    </form>
  );
}