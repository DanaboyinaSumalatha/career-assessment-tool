/**
 * Reusable Input component with label and error handling
 */
const Input = ({
  label,
  id,
  type = 'text',
  error,
  required = false,
  className = '',
  helpText,
  ...props
}) => (
  <div className={`flex flex-col gap-1.5 ${className}`}>
    {label && (
      <label htmlFor={id} className="text-sm font-medium text-gray-700">
        {label} {required && <span className="text-red-500">*</span>}
      </label>
    )}
    <input
      id={id}
      type={type}
      className={`w-full px-4 py-2.5 rounded-xl border text-sm bg-white transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent ${
        error
          ? 'border-red-400 bg-red-50 focus:ring-red-400'
          : 'border-gray-300 hover:border-gray-400'
      }`}
      {...props}
    />
    {helpText && !error && <p className="text-xs text-gray-500">{helpText}</p>}
    {error && <p className="text-xs text-red-500">{error}</p>}
  </div>
);

export const Select = ({ label, id, error, required = false, children, className = '', ...props }) => (
  <div className={`flex flex-col gap-1.5 ${className}`}>
    {label && (
      <label htmlFor={id} className="text-sm font-medium text-gray-700">
        {label} {required && <span className="text-red-500">*</span>}
      </label>
    )}
    <select
      id={id}
      className={`w-full px-4 py-2.5 rounded-xl border text-sm bg-white transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent ${
        error ? 'border-red-400 bg-red-50' : 'border-gray-300 hover:border-gray-400'
      }`}
      {...props}
    >
      {children}
    </select>
    {error && <p className="text-xs text-red-500">{error}</p>}
  </div>
);

export const Textarea = ({ label, id, error, required = false, className = '', ...props }) => (
  <div className={`flex flex-col gap-1.5 ${className}`}>
    {label && (
      <label htmlFor={id} className="text-sm font-medium text-gray-700">
        {label} {required && <span className="text-red-500">*</span>}
      </label>
    )}
    <textarea
      id={id}
      className={`w-full px-4 py-2.5 rounded-xl border text-sm bg-white transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none ${
        error ? 'border-red-400 bg-red-50' : 'border-gray-300 hover:border-gray-400'
      }`}
      {...props}
    />
    {error && <p className="text-xs text-red-500">{error}</p>}
  </div>
);

export default Input;
