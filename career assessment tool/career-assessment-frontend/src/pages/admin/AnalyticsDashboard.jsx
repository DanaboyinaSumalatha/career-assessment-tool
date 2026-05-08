import { useEffect, useState } from 'react';
import { adminService } from '../../services/adminService';
import PageHeader from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/Card';
import { Users, CheckCircle, TrendingUp, Briefcase } from 'lucide-react';
import {
  LineChart, Line, AreaChart, Area, BarChart, Bar,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend, RadarChart, Radar, PolarGrid,
  PolarAngleAxis, PolarRadiusAxis,
} from 'recharts';

const MOCK_ANALYTICS = {
  totalStudents: 1248,
  completionRate: 70,
  avgMatchScore: 82,
  activeCareerPaths: 48,
  registrationTrend: [
    { month: 'Sep', students: 80 }, { month: 'Oct', students: 120 },
    { month: 'Nov', students: 95 }, { month: 'Dec', students: 60 },
    { month: 'Jan', students: 200 }, { month: 'Feb', students: 180 },
  ],
  completionByGrade: [
    { grade: 'Grade 9', completed: 45, pending: 55 },
    { grade: 'Grade 10', completed: 60, pending: 40 },
    { grade: 'Grade 11', completed: 72, pending: 28 },
    { grade: 'Grade 12', completed: 85, pending: 15 },
    { grade: 'UG Year 1', completed: 55, pending: 45 },
    { grade: 'UG Year 2', completed: 78, pending: 22 },
  ],
  topCareers: [
    { career: 'Software Eng.', count: 320 },
    { career: 'Data Scientist', count: 260 },
    { career: 'UX Designer', count: 185 },
    { career: 'Product Mgr', count: 142 },
    { career: 'Healthcare', count: 130 },
    { career: 'Research', count: 98 },
  ],
  personalityDistribution: [
    { name: 'INTJ', value: 18 }, { name: 'ENTP', value: 15 },
    { name: 'INFP', value: 14 }, { name: 'ESTJ', value: 12 },
    { name: 'ISFJ', value: 11 }, { name: 'Other', value: 30 },
  ],
  avgSkills: [
    { skill: 'Programming', score: 72 }, { skill: 'Analysis', score: 68 },
    { skill: 'Communication', score: 75 }, { skill: 'Problem Solving', score: 80 },
    { skill: 'Leadership', score: 58 }, { skill: 'Creativity', score: 71 },
  ],
};

const COLORS = ['#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899'];

const AnalyticsDashboard = () => {
  const [analytics, setAnalytics] = useState(null);

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await adminService.getAnalytics();
        setAnalytics(data);
      } catch {
        setAnalytics(MOCK_ANALYTICS);
      }
    };
    fetch();
  }, []);

  const a = analytics || MOCK_ANALYTICS;

  return (
    <div>
      <PageHeader title="Analytics Dashboard" subtitle="Platform-wide data insights and trends." />

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard title="Total Students" value={a.totalStudents?.toLocaleString()} icon={Users} color="indigo" change={12} />
        <StatCard title="Completion Rate" value={`${a.completionRate}%`} icon={CheckCircle} color="green" change={5} />
        <StatCard title="Avg Match Score" value={`${a.avgMatchScore}%`} icon={TrendingUp} color="blue" change={3} />
        <StatCard title="Career Paths" value={a.activeCareerPaths} icon={Briefcase} color="purple" />
      </div>

      {/* Row 1 */}
      <div className="grid lg:grid-cols-2 gap-6 mb-6">
        <div className="bg-white rounded-2xl border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-800 mb-4">Student Registration Trend</h3>
          <ResponsiveContainer width="100%" height={220}>
            <AreaChart data={a.registrationTrend}>
              <defs>
                <linearGradient id="grad1" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="month" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Area type="monotone" dataKey="students" stroke="#6366f1" fill="url(#grad1)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-800 mb-4">Completion by Grade</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={a.completionByGrade}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="grade" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="completed" stackId="a" fill="#10b981" radius={[0, 0, 0, 0]} name="Completed" />
              <Bar dataKey="pending" stackId="a" fill="#e5e7eb" radius={[4, 4, 0, 0]} name="Pending" />
              <Legend />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Row 2 */}
      <div className="grid lg:grid-cols-3 gap-6 mb-6">
        <div className="bg-white rounded-2xl border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-800 mb-4">Personality Distribution</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={a.personalityDistribution} cx="50%" cy="50%" outerRadius={80} dataKey="value" paddingAngle={3}>
                {a.personalityDistribution?.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-800 mb-4">Average Skill Scores</h3>
          <ResponsiveContainer width="100%" height={220}>
            <RadarChart data={a.avgSkills}>
              <PolarGrid />
              <PolarAngleAxis dataKey="skill" tick={{ fontSize: 11 }} />
              <PolarRadiusAxis domain={[0, 100]} tick={{ fontSize: 9 }} />
              <Radar dataKey="score" stroke="#06b6d4" fill="#06b6d4" fillOpacity={0.3} />
              <Tooltip />
            </RadarChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-800 mb-4">Top Career Recommendations</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={a.topCareers} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis type="number" tick={{ fontSize: 11 }} />
              <YAxis dataKey="career" type="category" tick={{ fontSize: 11 }} width={90} />
              <Tooltip />
              <Bar dataKey="count" fill="#8b5cf6" radius={[0, 4, 4, 0]} name="Students" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};

export default AnalyticsDashboard;
