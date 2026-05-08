import { Link } from 'react-router-dom';
import { ShieldX } from 'lucide-react';
import Button from '../../components/common/Button';
import { useAuth } from '../../context/AuthContext';

const UnauthorizedPage = () => {
  const { isAdmin } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="text-center max-w-md">
        <div className="w-24 h-24 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
          <ShieldX size={48} className="text-red-500" />
        </div>
        <h1 className="text-3xl font-bold text-gray-900 mb-3">Access Denied</h1>
        <p className="text-gray-500 mb-8">
          You don't have permission to access this page. Please contact your administrator if you believe this is an error.
        </p>
        <Link to={isAdmin ? '/admin/dashboard' : '/student/dashboard'}>
          <Button size="lg">Go to Dashboard</Button>
        </Link>
      </div>
    </div>
  );
};

export default UnauthorizedPage;
