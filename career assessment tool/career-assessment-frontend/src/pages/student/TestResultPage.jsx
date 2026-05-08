import { useEffect, useState } from 'react';
import { assessmentService } from '../../services/assessmentService';
import PageHeader from '../../components/common/PageHeader';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Spinner from '../../components/common/Spinner';
import { EmptyState } from '../../components/common/PageHeader';
import {
  RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';
import { Brain, Wrench, Heart, CheckCircle, FileText } from 'lucide-react';

const MOCK_RESULTS = {
  personality: {
    status: 'completed',
    completedAt: '2026-02-20',
    scores: [
      { trait: 'Extraversion', score: 75 },
      { trait: 'Conscientiousness', score: 85 },
      { trait: 'Openness', score: 90 },
      { trait: 'Agreeableness', score: 70 },
      { trait: 'Neuroticism', score: 40 },
    ],
    type: 'INTJ',
    summary: 'Strategic thinker with strong analytical abilities. You are driven, imaginative, and decisive.',
  },
  skills: {
    status: 'completed',
    completedAt: '2026-02-22',
    scores: [
      { skill: 'Programming', score: 80 },
      { skill: 'Data Analysis', score: 70 },
      { skill: 'Communication', score: 65 },
      { skill: 'Problem Solving', score: 90 },
      { skill: 'Leadership', score: 60 },
      { skill: 'Creativity', score: 75 },
    ],
    summary: 'Strong technical and analytical skills with good creative thinking.',
  },
  interest: {
    status: 'pending',
    completedAt: null,
    scores: [],
    summary: null,
  },
};

const TestResultPage = () => {
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('personality');

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await assessmentService.getMyResults();
        setResults(data);
      } catch {
        setResults(MOCK_RESULTS);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>;

  const tabs = [
    { id: 'personality', label: 'Personality', icon: Brain, color: 'indigo' },
    { id: 'skills', label: 'Skills', icon: Wrench, color: 'blue' },
    { id: 'interest', label: 'Interests', icon: Heart, color: 'pink' },
  ];

  const currentResult = results?.[activeTab];

  return (
    <div>
      <PageHeader title="Assessment Results" subtitle="Detailed breakdown of your assessment performance." />

      {/* Summary cards */}
      <div className="grid sm:grid-cols-3 gap-4 mb-6">
        {tabs.map((tab) => {
          const r = results?.[tab.id];
          return (
            <Card key={tab.id}>
              <div className="flex items-center justify-between mb-3">
                <tab.icon size={20} className={`text-${tab.color}-500`} />
                <Badge variant={r?.status === 'completed' ? 'success' : 'warning'}>
                  {r?.status === 'completed' ? 'Completed' : 'Pending'}
                </Badge>
              </div>
              <h3 className="font-semibold text-gray-800 capitalize">{tab.label}</h3>
              {r?.completedAt && (
                <p className="text-xs text-gray-400 mt-1">
                  {new Date(r.completedAt).toLocaleDateString()}
                </p>
              )}
            </Card>
          );
        })}
      </div>

      {/* Tab navigation */}
      <div className="flex gap-2 mb-6 bg-gray-100 p-1 rounded-xl w-fit">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              activeTab === tab.id ? 'bg-white shadow-sm text-gray-900' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            <tab.icon size={16} />
            {tab.label}
          </button>
        ))}
      </div>

      {/* Result detail */}
      {currentResult?.status !== 'completed' ? (
        <EmptyState
          title="Not yet completed"
          description="Complete this assessment to see your results here."
          icon={FileText}
        />
      ) : (
        <div className="grid lg:grid-cols-2 gap-6">
          {/* Chart */}
          <Card>
            <h3 className="font-semibold text-gray-800 mb-4">Score Breakdown</h3>
            <ResponsiveContainer width="100%" height={280}>
              {activeTab === 'personality' ? (
                <RadarChart data={currentResult.scores}>
                  <PolarGrid />
                  <PolarAngleAxis dataKey="trait" tick={{ fontSize: 12 }} />
                  <PolarRadiusAxis angle={30} domain={[0, 100]} tick={{ fontSize: 10 }} />
                  <Radar name="Score" dataKey="score" stroke="#6366f1" fill="#6366f1" fillOpacity={0.3} />
                  <Tooltip />
                </RadarChart>
              ) : (
                <BarChart data={currentResult.scores}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="skill" tick={{ fontSize: 11 }} />
                  <YAxis domain={[0, 100]} tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Bar dataKey="score" fill="#6366f1" radius={[6, 6, 0, 0]} />
                </BarChart>
              )}
            </ResponsiveContainer>
          </Card>

          {/* Summary */}
          <div className="space-y-4">
            {activeTab === 'personality' && currentResult.type && (
              <Card>
                <h3 className="text-sm font-medium text-gray-500 mb-1">Personality Type</h3>
                <p className="text-4xl font-black text-indigo-600">{currentResult.type}</p>
              </Card>
            )}

            <Card>
              <h3 className="text-sm font-medium text-gray-500 mb-2">Summary</h3>
              <p className="text-gray-700 text-sm leading-relaxed">{currentResult.summary}</p>
            </Card>

            <Card>
              <h3 className="text-sm font-medium text-gray-500 mb-3">Scores</h3>
              <div className="space-y-3">
                {(currentResult.scores || []).map((item) => {
                  const key = item.trait || item.skill;
                  return (
                    <div key={key}>
                      <div className="flex justify-between text-sm mb-1">
                        <span className="text-gray-700 font-medium">{key}</span>
                        <span className="text-indigo-600 font-semibold">{item.score}%</span>
                      </div>
                      <div className="w-full bg-gray-100 rounded-full h-2">
                        <div
                          className="bg-indigo-500 h-2 rounded-full transition-all"
                          style={{ width: `${item.score}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
};

export default TestResultPage;
