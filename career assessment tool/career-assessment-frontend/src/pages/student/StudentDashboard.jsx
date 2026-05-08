import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { StatCard } from '../../components/common/Card';
import PageHeader from '../../components/common/PageHeader';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import { studentService } from '../../services/studentService';
import {
  Brain,
  Wrench,
  Heart,
  Star,
  FileText,
  ChevronRight,
  CheckCircle,
  Clock,
  AlertCircle,
} from 'lucide-react';

const assessmentCards = [
  {
    id: 'personality',
    title: 'Personality Test',
    description: 'Understand your personality type and behavioral traits.',
    icon: Brain,
    color: 'bg-purple-50 border-purple-100',
    iconColor: 'text-purple-600',
    path: '/student/personality-test',
    duration: '15 min',
  },
  {
    id: 'skills',
    title: 'Skills Assessment',
    description: 'Evaluate your technical and soft skills profile.',
    icon: Wrench,
    color: 'bg-blue-50 border-blue-100',
    iconColor: 'text-blue-600',
    path: '/student/skills-assessment',
    duration: '20 min',
  },
  {
    id: 'interest',
    title: 'Interest Survey',
    description: 'Discover your professional interests and passions.',
    icon: Heart,
    color: 'bg-pink-50 border-pink-100',
    iconColor: 'text-pink-600',
    path: '/student/interest-survey',
    duration: '10 min',
  },
];

const StudentDashboard = () => {
  const { user } = useAuth();
  const [dashData, setDashData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const { data } = await studentService.getDashboard();
        setDashData(data);
      } catch {
        // Use mock data if backend not available
        setDashData({
          completedTests: 1,
          totalTests: 3,
          recommendationsCount: 5,
          profileCompletion: 60,
          recentActivity: [
            { type: 'personality', completedAt: new Date().toISOString(), status: 'completed' },
          ],
          testStatus: { personality: 'completed', skills: 'pending', interest: 'pending' },
        });
      } finally {
        setLoading(false);
      }
    };
    fetchDashboard();
  }, []);

  const getStatusBadge = (status) => {
    if (status === 'completed') return <Badge variant="success">Completed</Badge>;
    if (status === 'in_progress') return <Badge variant="warning">In Progress</Badge>;
    return <Badge variant="default">Not Started</Badge>;
  };

  const getStatusIcon = (status) => {
    if (status === 'completed') return <CheckCircle size={18} className="text-green-500" />;
    if (status === 'in_progress') return <Clock size={18} className="text-yellow-500" />;
    return <AlertCircle size={18} className="text-gray-400" />;
  };

  return (
    <div>
      <PageHeader
        title={`Welcome, ${user?.firstName}! 👋`}
        subtitle="Track your career assessment progress and discover your path."
      />

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard title="Tests Completed" value={`${dashData?.completedTests ?? 0}/3`} icon={CheckCircle} color="green" />
        <StatCard title="Recommendations" value={dashData?.recommendationsCount ?? 0} icon={Star} color="indigo" />
        <StatCard title="Profile Completion" value={`${dashData?.profileCompletion ?? 0}%`} icon={FileText} color="blue" />
        <StatCard title="Tests Remaining" value={(3 - (dashData?.completedTests ?? 0))} icon={Clock} color="orange" />
      </div>

      {/* Assessment cards */}
      <div className="mb-8">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Your Assessments</h2>
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {assessmentCards.map((card) => {
            const status = dashData?.testStatus?.[card.id] ?? 'pending';
            return (
              <div key={card.id} className={`rounded-2xl border p-6 ${card.color} transition-all hover:shadow-md`}>
                <div className="flex items-start justify-between mb-4">
                  <div className={`w-12 h-12 bg-white rounded-xl flex items-center justify-center shadow-sm`}>
                    <card.icon size={24} className={card.iconColor} />
                  </div>
                  {getStatusBadge(status)}
                </div>
                <h3 className="font-semibold text-gray-900 mb-1">{card.title}</h3>
                <p className="text-gray-500 text-sm mb-1">{card.description}</p>
                <p className="text-xs text-gray-400 mb-4">⏱ {card.duration}</p>
                <Link to={card.path}>
                  <Button
                    size="sm"
                    variant={status === 'completed' ? 'secondary' : 'primary'}
                    fullWidth
                  >
                    {status === 'completed' ? 'Review' : 'Start Test'}
                    <ChevronRight size={16} />
                  </Button>
                </Link>
              </div>
            );
          })}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid sm:grid-cols-2 gap-4">
        <div className="bg-gradient-to-r from-indigo-600 to-purple-600 rounded-2xl p-6 text-white">
          <Star size={32} className="mb-3 text-yellow-300" />
          <h3 className="font-semibold text-lg mb-2">View Recommendations</h3>
          <p className="text-indigo-100 text-sm mb-4">See career paths matched to your profile.</p>
          <Link to="/student/recommendations">
            <Button variant="outline" className="border-white text-white hover:bg-white/10" size="sm">
              View Careers <ChevronRight size={16} />
            </Button>
          </Link>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6">
          <FileText size={32} className="mb-3 text-indigo-500" />
          <h3 className="font-semibold text-lg text-gray-900 mb-2">Test Results</h3>
          <p className="text-gray-500 text-sm mb-4">Review your detailed assessment results and analysis.</p>
          <Link to="/student/results">
            <Button size="sm">
              View Results <ChevronRight size={16} />
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default StudentDashboard;
