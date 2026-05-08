import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { GraduationCap, LogOut, ArrowLeft, CheckCircle } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const LogoutPage = () => {
  const { logout, user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [status, setStatus] = useState('confirm'); // 'confirm' | 'loading' | 'done'

  // If already logged out, redirect immediately
  if (!isAuthenticated && status === 'confirm') {
    navigate('/login', { replace: true });
    return null;
  }

  const handleLogout = async () => {
    setStatus('loading');
    await logout();
    setStatus('done');
    // Auto-redirect after 2 s
    setTimeout(() => navigate('/login', { replace: true }), 2000);
  };

  const handleCancel = () => {
    navigate(-1);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md">

        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 text-indigo-600 font-bold text-2xl">
            <GraduationCap size={32} />
            CareerPath
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">

          {/* ── Confirm state ─────────────────────────────────────────── */}
          {status === 'confirm' && (
            <>
              {/* Icon */}
              <div className="flex justify-center mb-6">
                <div className="w-16 h-16 bg-red-50 rounded-2xl flex items-center justify-center">
                  <LogOut size={30} className="text-red-500" />
                </div>
              </div>

              {/* Heading */}
              <h2 className="text-2xl font-bold text-gray-900 text-center mb-1">
                Sign out?
              </h2>

              {/* User info */}
              {user && (
                <div className="flex items-center gap-3 bg-gray-50 rounded-xl px-4 py-3 mt-4 mb-6">
                  <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center font-bold text-indigo-600 text-sm flex-shrink-0">
                    {user.firstName?.[0]}{user.lastName?.[0]}
                  </div>
                  <div className="min-w-0">
                    <p className="font-semibold text-gray-800 text-sm truncate">
                      {user.firstName} {user.lastName}
                    </p>
                    <p className="text-xs text-gray-400 truncate">{user.email}</p>
                    <span className="inline-block mt-0.5 text-[10px] font-medium px-2 py-0.5 rounded-full bg-indigo-100 text-indigo-600 uppercase tracking-wide">
                      {user.role}
                    </span>
                  </div>
                </div>
              )}

              <p className="text-gray-500 text-sm text-center mb-8">
                You will be signed out of your account. Your progress is saved and you can sign back in any time.
              </p>

              {/* Actions */}
              <div className="space-y-3">
                <button
                  onClick={handleLogout}
                  className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-red-500 hover:bg-red-600 text-white font-semibold rounded-xl transition-all shadow-sm hover:shadow"
                >
                  <LogOut size={18} />
                  Yes, Sign Out
                </button>
                <button
                  onClick={handleCancel}
                  className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-xl transition-all"
                >
                  <ArrowLeft size={18} />
                  Go Back
                </button>
              </div>
            </>
          )}

          {/* ── Loading state ─────────────────────────────────────────── */}
          {status === 'loading' && (
            <div className="flex flex-col items-center justify-center py-8 gap-4">
              <div className="w-14 h-14 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin" />
              <p className="text-gray-600 font-medium">Signing you out…</p>
            </div>
          )}

          {/* ── Done state ────────────────────────────────────────────── */}
          {status === 'done' && (
            <div className="flex flex-col items-center justify-center py-8 gap-4">
              <div className="w-16 h-16 bg-green-50 rounded-2xl flex items-center justify-center">
                <CheckCircle size={36} className="text-green-500" />
              </div>
              <h3 className="text-xl font-bold text-gray-900">Signed Out</h3>
              <p className="text-gray-500 text-sm text-center">
                You've been successfully signed out.
                <br />Redirecting to login…
              </p>
              <div className="w-32 h-1 bg-gray-100 rounded-full overflow-hidden mt-2">
                <div className="h-full bg-indigo-500 rounded-full animate-[grow_2s_linear_forwards]" />
              </div>
            </div>
          )}

        </div>

        {/* Footer link */}
        {status === 'confirm' && (
          <p className="text-center text-sm text-gray-500 mt-6">
            Changed your mind?{' '}
            <button
              onClick={handleCancel}
              className="text-indigo-600 font-medium hover:underline"
            >
              Stay signed in
            </button>
          </p>
        )}

        {status === 'done' && (
          <p className="text-center text-sm text-gray-500 mt-6">
            <button
              onClick={() => navigate('/login', { replace: true })}
              className="text-indigo-600 font-medium hover:underline"
            >
              Go to Login now →
            </button>
          </p>
        )}
      </div>
    </div>
  );
};

export default LogoutPage;
