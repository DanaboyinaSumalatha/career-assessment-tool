import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { GraduationCap, Eye, EyeOff, Shield } from 'lucide-react';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';

const AdminLoginPage = () => {
  const { adminLogin } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const validate = () => {
    const errs = {};
    if (!form.email) errs.email = 'Email is required';
    if (!form.password) errs.password = 'Password is required';
    return errs;
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    const result = await adminLogin(form);
    setLoading(false);
    if (result.success) navigate('/admin/dashboard', { replace: true });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-indigo-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-indigo-500 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-xl">
            <Shield size={32} className="text-white" />
          </div>
          <div className="flex items-center justify-center gap-2 text-white font-bold text-2xl mb-2">
            <GraduationCap size={28} className="text-indigo-400" />
            CareerPath
          </div>
          <p className="text-gray-400">Administrator Portal</p>
        </div>

        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Admin Sign In</h2>
          <form onSubmit={handleSubmit} className="space-y-5">
            <Input
              label="Admin Email"
              id="email"
              name="email"
              type="email"
              placeholder="danaboyinamahendra@gmail.com"
              
              value={form.email}
              onChange={handleChange}
              error={errors.email}
              required
            />

            <div className="flex flex-col gap-1.5">
              <label htmlFor="password" className="text-sm font-medium text-gray-700">
                Password <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={handleChange}
                  className={`w-full px-4 py-2.5 rounded-xl border text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 pr-12 ${
                    errors.password ? 'border-red-400' : 'border-gray-300'
                  }`}
                />
                <button
                  type="button"
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {errors.password && <p className="text-xs text-red-500">{errors.password}</p>}
            </div>

            <Button type="submit" fullWidth size="lg" loading={loading}>
              Sign In to Admin Panel
            </Button>
          </form>
        </div>

        <p className="text-center text-sm text-gray-400 mt-6">
          <Link to="/login" className="hover:text-white transition-colors">← Back to Student Login</Link>
        </p>
      </div>
    </div>
  );
};

export default AdminLoginPage;
