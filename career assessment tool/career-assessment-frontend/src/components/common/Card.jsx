/**
 * Reusable Card component
 */
const Card = ({ children, className = '', padding = true }) => (
  <div className={`bg-white rounded-2xl shadow-sm border border-gray-100 ${padding ? 'p-6' : ''} ${className}`}>
    {children}
  </div>
);

export const StatCard = ({ title, value, icon: Icon, color = 'indigo', change }) => {
  const colors = {
    indigo: 'bg-indigo-50 text-indigo-600',
    green: 'bg-green-50 text-green-600',
    blue: 'bg-blue-50 text-blue-600',
    purple: 'bg-purple-50 text-purple-600',
    orange: 'bg-orange-50 text-orange-600',
  };

  return (
    <Card>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-gray-500 font-medium">{title}</p>
          <p className="text-3xl font-bold text-gray-900 mt-1">{value}</p>
          {change && (
            <p className={`text-xs mt-1 ${change >= 0 ? 'text-green-600' : 'text-red-500'}`}>
              {change >= 0 ? '▲' : '▼'} {Math.abs(change)}% from last month
            </p>
          )}
        </div>
        {Icon && (
          <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${colors[color]}`}>
            <Icon size={24} />
          </div>
        )}
      </div>
    </Card>
  );
};

export default Card;
