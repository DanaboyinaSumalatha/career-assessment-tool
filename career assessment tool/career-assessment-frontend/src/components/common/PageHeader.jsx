import { AlertTriangle } from 'lucide-react';

const PageHeader = ({ title, subtitle, children }) => (
  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
    <div>
      <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
      {subtitle && <p className="text-gray-500 mt-1 text-sm">{subtitle}</p>}
    </div>
    {children && <div className="flex items-center gap-3">{children}</div>}
  </div>
);

export const EmptyState = ({ title, description, icon: Icon, action }) => (
  <div className="flex flex-col items-center justify-center py-16 text-center">
    {Icon ? (
      <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
        <Icon size={32} className="text-gray-400" />
      </div>
    ) : (
      <AlertTriangle size={48} className="text-gray-300 mb-4" />
    )}
    <h3 className="text-lg font-semibold text-gray-700 mb-2">{title}</h3>
    {description && <p className="text-gray-400 text-sm max-w-sm">{description}</p>}
    {action && <div className="mt-6">{action}</div>}
  </div>
);

export default PageHeader;
