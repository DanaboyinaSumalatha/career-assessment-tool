import { useEffect, useState } from 'react';
import { adminService } from '../../services/adminService';
import { StatCard } from '../../components/common/Card';
import PageHeader from '../../components/common/PageHeader';
import { Users, Briefcase, CheckCircle, Clock } from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';

const COLORS = ['#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#f97316', '#84cc16'];

const AdminDashboard = () => {
  const [stats, setStats]         = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [students, setStudents]   = useState([]);
  const [loading, setLoading]     = useState(true);

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const [statsRes, analyticsRes, studentsRes] = await Promise.allSettled([
          adminService.getDashboardStats(),
          adminService.getAnalytics(),
          adminService.getAllStudents(),
        ]);
        if (statsRes.status === 'fulfilled')     setStats(statsRes.value.data);
        if (analyticsRes.status === 'fulfilled') setAnalytics(analyticsRes.value.data);
        if (studentsRes.status === 'fulfilled') {
          const sorted = [...(studentsRes.value.data ?? [])].sort(
            (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
          );
          setStudents(sorted.slice(0, 5));
        }
      } catch {
        // silently fall through
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, []);

  const s = stats ?? {};

  return (
    <div>
      <PageHeader title="Admin Dashboard" subtitle="Platform overview and key metrics." />

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard title="Total Students"    value={(s.totalStudents ?? 0).toLocaleString()}       icon={Users}         color="indigo" />
        <StatCard title="Assessments Done"  value={(s.completedAssessments ?? 0).toLocaleString()} icon={CheckCircle}  color="green"  />
        <StatCard title="New This Month"    value={(s.newStudentsThisMonth ?? 0).toLocaleString()} icon={Clock}        color="blue"   />
        <StatCard title="Career Paths"      value={s.totalCareerPaths ?? 0}                        icon={Briefcase}    color="purple" />
      </div>

      {/* Completion Rates */}
      {stats && (
        <div className="bg-white rounded-2xl border border-gray-100 p-6 mb-6">
          <h3 className="font-semibold text-gray-800 mb-4">Assessment Completion Rates</h3>
          <div className="space-y-3">
            {[
              { label: 'Personality', rate: s.personalityCompletionRate ?? 0, color: 'bg-purple-500' },
              { label: 'Skills',      rate: s.skillsCompletionRate      ?? 0, color: 'bg-blue-500'   },
              { label: 'Interest',    rate: s.interestCompletionRate    ?? 0, color: 'bg-pink-500'   },
            ].map(({ label, rate, color }) => (
              <div key={label}>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600">{label}</span>
                  <span className="font-medium text-gray-800">{Number(rate).toFixed(1)}%</span>
                </div>
                <div className="w-full bg-gray-100 rounded-full h-2">
                  <div className={`${color} h-2 rounded-full`} style={{ width: `${rate}%` }} />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Charts */}
      <div className="grid lg:grid-cols-2 gap-6 mb-8">
        <div className="bg-white rounded-2xl border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-800 mb-5">Monthly Registrations</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={analytics?.monthlyRegistrations ?? []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="month" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="students" fill="#6366f1" radius={[6, 6, 0, 0]} name="Students" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-800 mb-5">Career Interest Distribution</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie
                data={analytics?.careerDistribution ?? []}
                cx="50%" cy="50%"
                innerRadius={60} outerRadius={90}
                paddingAngle={4} dataKey="value"
              >
                {(analytics?.careerDistribution ?? []).map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Recent Students */}
      <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 className="font-semibold text-gray-800">Recent Students</h3>
          <a href="/admin/students" className="text-indigo-600 text-sm font-medium hover:underline">View All →</a>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                {['Name', 'Email', 'Grade', 'City', 'Joined'].map((h) => (
                  <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-gray-500 uppercase">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {students.length > 0 ? students.map((student) => (
                <tr key={student.id} className="hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-900">{student.firstName} {student.lastName}</td>
                  <td className="px-5 py-3 text-gray-500">{student.email}</td>
                  <td className="px-5 py-3 text-gray-600">{student.grade ?? '—'}</td>
                  <td className="px-5 py-3 text-gray-600">{student.city ?? '—'}</td>
                  <td className="px-5 py-3 text-gray-400 text-xs">
                    {student.createdAt ? new Date(student.createdAt).toLocaleDateString() : '—'}
                  </td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={5} className="px-5 py-8 text-center text-gray-400">
                    {loading ? 'Loading...' : 'No students yet.'}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
