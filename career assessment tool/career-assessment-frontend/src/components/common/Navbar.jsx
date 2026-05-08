import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { GraduationCap, LogOut, User, Menu, X } from 'lucide-react';
import { useState } from 'react';

const Navbar = () => {
  const { isAuthenticated, user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const dashboardPath = isAdmin ? '/admin/dashboard' : '/student/dashboard';

  return (
    <nav className="bg-white shadow-sm border-b border-gray-100 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 text-indigo-600 font-bold text-xl">
            <GraduationCap size={28} />
            <span className="hidden sm:block">CareerPath</span>
          </Link>

          {/* Desktop nav */}
          <div className="hidden md:flex items-center gap-6">
            {!isAuthenticated ? (
              <>
                <Link to="/" className="text-gray-600 hover:text-indigo-600 font-medium transition-colors">Home</Link>
                <Link to="/login" className="text-gray-600 hover:text-indigo-600 font-medium transition-colors">Login</Link>
                <Link to="/register" className="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors font-medium">Get Started</Link>
              </>
            ) : (
              <>
                <Link to={dashboardPath} className="text-gray-600 hover:text-indigo-600 font-medium transition-colors">Dashboard</Link>
                <div className="flex items-center gap-3">
                  <Link to={isAdmin ? '/admin/dashboard' : '/student/profile'} className="flex items-center gap-2 text-gray-700 hover:text-indigo-600 transition-colors">
                    <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center">
                      <User size={16} className="text-indigo-600" />
                    </div>
                    <span className="font-medium text-sm">{user?.firstName}</span>
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 text-red-500 hover:text-red-700 transition-colors font-medium text-sm"
                  >
                    <LogOut size={16} />
                    Logout
                  </button>
                </div>
              </>
            )}
          </div>

          {/* Mobile hamburger */}
          <button className="md:hidden p-2 text-gray-600" onClick={() => setMenuOpen(!menuOpen)}>
            {menuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {/* Mobile menu */}
        {menuOpen && (
          <div className="md:hidden pb-4 space-y-2 border-t border-gray-100 pt-3">
            {!isAuthenticated ? (
              <>
                <Link to="/" className="block px-4 py-2 text-gray-700 hover:bg-indigo-50 rounded-lg" onClick={() => setMenuOpen(false)}>Home</Link>
                <Link to="/login" className="block px-4 py-2 text-gray-700 hover:bg-indigo-50 rounded-lg" onClick={() => setMenuOpen(false)}>Login</Link>
                <Link to="/register" className="block px-4 py-2 bg-indigo-600 text-white rounded-lg text-center" onClick={() => setMenuOpen(false)}>Get Started</Link>
              </>
            ) : (
              <>
                <Link to={dashboardPath} className="block px-4 py-2 text-gray-700 hover:bg-indigo-50 rounded-lg" onClick={() => setMenuOpen(false)}>Dashboard</Link>
                <button onClick={() => { handleLogout(); setMenuOpen(false); }} className="block w-full text-left px-4 py-2 text-red-500 hover:bg-red-50 rounded-lg">Logout</button>
              </>
            )}
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
