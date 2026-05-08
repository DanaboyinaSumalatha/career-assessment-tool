import { useEffect, useState } from 'react';
import { adminService } from '../../services/adminService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Spinner from '../../components/common/Spinner';
import { Search, Eye } from 'lucide-react';
import {
  RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';

const MOCK_STUDENTS = [
  { id: 1, name: 'Alice Johnson', email: 'alice@demo.com', grade: '12', personalityStatus: 'completed', skillsStatus: 'completed', interestStatus: 'completed', joinedAt: '2026-02-10' },
  { id: 2, name: 'Bob Smith', email: 'bob@demo.com', grade: '11', personalityStatus: 'completed', skillsStatus: 'in_progress', interestStatus: 'pending', joinedAt: '2026-02-12' },
  { id: 3, name: 'Carol Davis', email: 'carol@demo.com', grade: 'undergrad_2', personalityStatus: 'completed', skillsStatus: 'completed', interestStatus: 'pending', joinedAt: '2026-02-15' },
  { id: 4, name: 'David Lee', email: 'david@demo.com', grade: '10', personalityStatus: 'pending', skillsStatus: 'pending', interestStatus: 'pending', joinedAt: '2026-02-18' },
  { id: 5, name: 'Eva Martinez', email: 'eva@demo.com', grade: '12', personalityStatus: 'completed', skillsStatus: 'completed', interestStatus: 'completed', joinedAt: '2026-02-20' },
];

const MOCK_RESULT = {
  personality: {
    status: 'completed',
    type: 'INTJ',
    scores: [
      { trait: 'Extraversion', score: 75 }, { trait: 'Conscientiousness', score: 85 },
      { trait: 'Openness', score: 90 }, { trait: 'Agreeableness', score: 70 }, { trait: 'Emotional Stability', score: 40 },
    ],
  },
  skills: {
    status: 'completed',
    scores: [
      { skill: 'Programming', score: 80 }, { skill: 'Data Analysis', score: 70 },
      { skill: 'Communication', score: 65 }, { skill: 'Problem Solving', score: 90 },
    ],
  },
  interest: {
    status: 'completed',
    scores: [
      { skill: 'Technology', score: 85 }, { skill: 'Science', score: 70 },
    ],
  },
  topRecommendation: 'Software Engineer',
};

const statusBadge = (v) => {
  if (v === 'completed') return <Badge variant="success">Done</Badge>;
  if (v === 'in_progress') return <Badge variant="warning">In Progress</Badge>;
  return <Badge variant="default">Pending</Badge>;
};

const StudentResultsViewer = () => {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState(null);
  const [results, setResults] = useState(null);
  const [resultsLoading, setResultsLoading] = useState(false);

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await adminService.getAllStudents();
        setStudents(data);
      } catch {
        setStudents(MOCK_STUDENTS);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  const handleView = async (student) => {
    setSelected(student);
    setResultsLoading(true);
    try {
      const { data } = await adminService.getStudentResults(student.id);
      setResults(data);
    } catch {
      setResults(MOCK_RESULT);
    } finally {
      setResultsLoading(false);
    }
  };

  const filtered = students.filter((s) => {
    const fullName = `${s.firstName ?? ''} ${s.lastName ?? ''}`.toLowerCase();
    return (
      fullName.includes(search.toLowerCase()) ||
      (s.email ?? '').toLowerCase().includes(search.toLowerCase())
    );
  });

  const columns = [
    { key: 'firstName', label: 'Student', render: (v, row) => (
      <div>
        <p className="font-medium text-gray-900">{row.firstName} {row.lastName}</p>
        <p className="text-xs text-gray-400">{row.email}</p>
      </div>
    )},
    { key: 'grade', label: 'Grade' },
    { key: 'personalityStatus', label: 'Personality', render: statusBadge },
    { key: 'skillsStatus', label: 'Skills', render: statusBadge },
    { key: 'interestStatus', label: 'Interest', render: statusBadge },
    { key: 'createdAt', label: 'Joined', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
    { key: 'actions', label: 'View', render: (_, row) => (
      <button onClick={() => handleView(row)} className="flex items-center gap-1 text-indigo-600 hover:text-indigo-700 font-medium text-sm">
        <Eye size={15} /> View
      </button>
    )},
  ];

  return (
    <div>
      <PageHeader title="Student Results" subtitle="View and analyze individual student assessment results." />

      <div className="relative mb-6 max-w-sm">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="Search students..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <Table columns={columns} data={filtered} loading={loading} />

      {/* Detail Modal */}
      <Modal isOpen={!!selected} onClose={() => setSelected(null)} title={`Results: ${selected?.firstName ?? ''} ${selected?.lastName ?? ''}`} size="xl">
        {resultsLoading ? (
          <div className="flex justify-center py-10"><Spinner size="lg" /></div>
        ) : results ? (
          <div className="space-y-6">
            <div className="flex gap-4 flex-wrap">
              <div className="bg-indigo-50 rounded-xl p-4 flex-1 min-w-[150px]">
                <p className="text-xs text-gray-500">Personality Type</p>
                <p className="text-3xl font-black text-indigo-600">{results.personality?.type ?? '—'}</p>
              </div>
              <div className="bg-green-50 rounded-xl p-4 flex-1 min-w-[150px]">
                <p className="text-xs text-gray-500">Top Recommendation</p>
                <p className="text-lg font-bold text-green-700">{results.topRecommendation ?? 'Not yet assessed'}</p>
              </div>
            </div>

            <div className="grid sm:grid-cols-2 gap-6">
              <div>
                <h4 className="font-semibold text-gray-700 mb-3 text-sm">Personality Traits</h4>
                {results.personality?.scores?.length > 0 ? (
                  <ResponsiveContainer width="100%" height={200}>
                    <RadarChart data={results.personality.scores}>
                      <PolarGrid />
                      <PolarAngleAxis dataKey="trait" tick={{ fontSize: 10 }} />
                      <PolarRadiusAxis domain={[0, 100]} tick={{ fontSize: 9 }} />
                      <Radar dataKey="score" stroke="#6366f1" fill="#6366f1" fillOpacity={0.3} />
                      <Tooltip />
                    </RadarChart>
                  </ResponsiveContainer>
                ) : (
                  <p className="text-sm text-gray-400 py-4">Personality test not completed.</p>
                )}
              </div>
              <div>
                <h4 className="font-semibold text-gray-700 mb-3 text-sm">Skills Profile</h4>
                {results.skills?.scores?.length > 0 ? (
                  <ResponsiveContainer width="100%" height={200}>
                    <BarChart data={results.skills.scores} layout="vertical">
                      <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                      <XAxis type="number" domain={[0, 100]} tick={{ fontSize: 10 }} />
                      <YAxis dataKey="skill" type="category" tick={{ fontSize: 10 }} width={90} />
                      <Tooltip />
                      <Bar dataKey="score" fill="#6366f1" radius={[0, 4, 4, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <p className="text-sm text-gray-400 py-4">Skills assessment not completed.</p>
                )}
              </div>
            </div>

            {results.interest?.scores?.length > 0 && (
              <div>
                <h4 className="font-semibold text-gray-700 mb-3 text-sm">Interest Areas</h4>
                <ResponsiveContainer width="100%" height={180}>
                  <BarChart data={results.interest.scores} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis type="number" domain={[0, 100]} tick={{ fontSize: 10 }} />
                    <YAxis dataKey="skill" type="category" tick={{ fontSize: 10 }} width={90} />
                    <Tooltip />
                    <Bar dataKey="score" fill="#10b981" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        ) : (
          <p className="text-gray-500 text-center py-6">No results available for this student.</p>
        )}
      </Modal>
    </div>
  );
};

export default StudentResultsViewer;
