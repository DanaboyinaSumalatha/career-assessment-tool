import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { GraduationCap, Menu, X, Bell, LogOut } from 'lucide-react';
import StudentSidebar from './StudentSidebar';
import { useAuth } from '../../context/AuthContext';

const StudentLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    navigate('/logout');
  };

  return (
    <div className="flex h-screen bg-gray-50 overflow-hidden">
      {/* Desktop Sidebar */}
      <aside className="hidden lg:flex w-64 h-screen sticky top-0 flex-shrink-0 bg-white border-r border-gray-100 flex-col">
        <div className="flex items-center gap-2 px-6 py-5 border-b border-gray-100">
          <GraduationCap size={26} className="text-indigo-600" />
          <span className="font-bold text-lg text-gray-800">CareerPath</span>
        </div>
        <div className="px-4 py-4 border-b border-gray-100">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 bg-indigo-100 rounded-full flex items-center justify-center font-bold text-indigo-600 text-sm">
              {user?.firstName?.[0]}{user?.lastName?.[0]}
            </div>
            <div>
              <p className="font-medium text-sm text-gray-800">{user?.firstName} {user?.lastName}</p>
              <p className="text-xs text-gray-400">Student</p>
            </div>
          </div>
        </div>
        <div className="flex-1 py-4 overflow-y-auto">
          <StudentSidebar />
        </div>
        <div className="p-4 border-t border-gray-100">
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-gray-500 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all"
          >
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </aside>

      {/* Mobile Sidebar */}
      {sidebarOpen && (
        <div className="lg:hidden fixed inset-0 z-50 flex">
          <div className="fixed inset-0 bg-black/50" onClick={() => setSidebarOpen(false)} />
          <aside className="relative w-64 bg-white flex flex-col h-full shadow-xl">
            <div className="flex items-center justify-between px-6 py-5 border-b border-gray-100">
              <div className="flex items-center gap-2">
                <GraduationCap size={24} className="text-indigo-600" />
                <span className="font-bold text-gray-800">CareerPath</span>
              </div>
              <button onClick={() => setSidebarOpen(false)} className="text-gray-500">
                <X size={20} />
              </button>
            </div>
            <div className="flex-1 py-4 overflow-y-auto">
              <StudentSidebar onClose={() => setSidebarOpen(false)} />
            </div>
            <div className="p-4 border-t">
              <button onClick={handleLogout} className="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-gray-500 hover:text-red-500 rounded-xl">
                <LogOut size={18} /> Logout
              </button>
            </div>
          </aside>
        </div>
      )}

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0 overflow-auto">
        <header className="sticky top-0 z-40 bg-white border-b border-gray-100 px-4 sm:px-6 py-3 flex items-center justify-between shadow-sm">
          <button className="lg:hidden p-2 rounded-lg hover:bg-gray-100" onClick={() => setSidebarOpen(true)}>
            <Menu size={22} className="text-gray-600" />
          </button>
          <h1 className="hidden lg:block text-sm font-medium text-gray-600">
            Welcome back, <span className="text-gray-900 font-semibold">{user?.firstName}!</span>
          </h1>
          <div className="flex items-center gap-3">
            <button className="relative p-2 rounded-full hover:bg-gray-100 text-gray-600">
              <Bell size={20} />
            </button>
            <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center text-indigo-700 font-bold text-sm">
              {user?.firstName?.[0]}
            </div>
          </div>
        </header>
        <main className="flex-1 p-4 sm:p-6 lg:p-8 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default StudentLayout;
