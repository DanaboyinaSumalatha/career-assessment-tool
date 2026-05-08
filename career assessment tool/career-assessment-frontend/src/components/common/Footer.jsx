import { GraduationCap, Mail, Phone, MapPin } from 'lucide-react';
import { Link } from 'react-router-dom';

const Footer = () => (
  <footer className="bg-gray-900 text-gray-300 mt-auto">
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
        {/* Brand */}
        <div className="col-span-1 md:col-span-2">
          <div className="flex items-center gap-2 text-white font-bold text-xl mb-4">
            <GraduationCap size={28} className="text-indigo-400" />
            CareerPath
          </div>
          <p className="text-gray-400 text-sm leading-relaxed max-w-xs">
            Empowering students to discover their ideal career paths through intelligent personality, skills, and interest assessments.
          </p>
        </div>

        {/* Quick links */}
        <div>
          <h3 className="text-white font-semibold mb-4">Quick Links</h3>
          <ul className="space-y-2 text-sm">
            <li><Link to="/" className="hover:text-indigo-400 transition-colors">Home</Link></li>
            <li><Link to="/login" className="hover:text-indigo-400 transition-colors">Student Login</Link></li>
            <li><Link to="/register" className="hover:text-indigo-400 transition-colors">Register</Link></li>
            <li><Link to="/admin/login" className="hover:text-indigo-400 transition-colors">Admin Portal</Link></li>
          </ul>
        </div>

        {/* Contact */}
        <div>
          <h3 className="text-white font-semibold mb-4">Contact</h3>
          <ul className="space-y-2 text-sm">
            <li className="flex items-center gap-2"><Mail size={14} /> support@careerpath.edu</li>
            <li className="flex items-center gap-2"><Phone size={14} /> +1 (800) 555-0199</li>
            <li className="flex items-center gap-2"><MapPin size={14} /> Education City, USA</li>
          </ul>
        </div>
      </div>

      <div className="border-t border-gray-800 mt-8 pt-6 flex flex-col sm:flex-row justify-between items-center text-sm text-gray-500">
        <p>© {new Date().getFullYear()} CareerPath. All rights reserved.</p>
        <p className="mt-2 sm:mt-0">Built with ❤️ for students</p>
      </div>
    </div>
  </footer>
);

export default Footer;
