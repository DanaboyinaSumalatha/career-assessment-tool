import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Brain,
  Wrench,
  Heart,
  FileText,
  Star,
  UserCircle,
  LogOut,
} from 'lucide-react';

const navItems = [
  { to: '/student/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/student/personality-test', icon: Brain, label: 'Personality Test' },
  { to: '/student/skills-assessment', icon: Wrench, label: 'Skills Assessment' },
  { to: '/student/interest-survey', icon: Heart, label: 'Interest Survey' },
  { to: '/student/results', icon: FileText, label: 'My Results' },
  { to: '/student/recommendations', icon: Star, label: 'Recommendations' },
  { to: '/student/profile', icon: UserCircle, label: 'Profile' },
];

const StudentSidebar = ({ onClose }) => (
  <nav className="space-y-1 px-2">
    {navItems.map(({ to, icon: Icon, label }) => (
      <NavLink
        key={to}
        to={to}
        onClick={onClose}
        className={({ isActive }) =>
          `flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all ${
            isActive
              ? 'bg-indigo-600 text-white shadow-sm'
              : 'text-gray-600 hover:bg-indigo-50 hover:text-indigo-700'
          }`
        }
      >
        <Icon size={18} />
        {label}
      </NavLink>
    ))}

    {/* Divider */}
    <div className="mx-4 my-2 border-t border-gray-100" />

    {/* Sign Out */}
    <NavLink
      to="/logout"
      onClick={onClose}
      className="flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all text-gray-500 hover:bg-red-50 hover:text-red-600"
    >
      <LogOut size={18} />
      Sign Out
    </NavLink>
  </nav>
);

export default StudentSidebar;
