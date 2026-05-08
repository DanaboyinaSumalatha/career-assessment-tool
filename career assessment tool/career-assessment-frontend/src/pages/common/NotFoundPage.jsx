import { Link } from 'react-router-dom';
import { Home } from 'lucide-react';
import Button from '../../components/common/Button';

const NotFoundPage = () => (
  <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
    <div className="text-center max-w-md">
      <p className="text-9xl font-black text-indigo-600 leading-none">404</p>
      <h1 className="text-3xl font-bold text-gray-900 mt-4 mb-3">Page Not Found</h1>
      <p className="text-gray-500 mb-8">
        The page you're looking for doesn't exist or has been moved.
      </p>
      <Link to="/">
        <Button size="lg">
          <Home size={18} /> Back to Home
        </Button>
      </Link>
    </div>
  </div>
);

export default NotFoundPage;
