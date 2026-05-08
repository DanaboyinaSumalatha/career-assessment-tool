import { useEffect, useState } from 'react';
import { assessmentService } from '../../services/assessmentService';
import PageHeader from '../../components/common/PageHeader';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Spinner from '../../components/common/Spinner';
import { EmptyState } from '../../components/common/PageHeader';
import {
  Star, Briefcase, TrendingUp, BookOpen, DollarSign, MapPin, ChevronRight,
} from 'lucide-react';

const MOCK_RECOMMENDATIONS = [
  {
    id: 1,
    title: 'Software Engineer',
    matchScore: 94,
    description: 'Design and develop software systems, applications and solutions.',
    salaryRange: '$85,000 – $150,000',
    growthRate: '+25%',
    requiredSkills: ['Programming', 'Problem Solving', 'System Design', 'Algorithms'],
    education: "Bachelor's in Computer Science",
    industry: 'Technology',
    workStyle: 'Remote/Hybrid',
    color: 'indigo',
  },
  {
    id: 2,
    title: 'Data Scientist',
    matchScore: 88,
    description: 'Analyze complex data to extract insights and drive business decisions.',
    salaryRange: '$90,000 – $160,000',
    growthRate: '+35%',
    requiredSkills: ['Data Analysis', 'Machine Learning', 'Statistics', 'Python'],
    education: "Master's in Data Science / Statistics",
    industry: 'Technology / Finance',
    workStyle: 'Hybrid',
    color: 'blue',
  },
  {
    id: 3,
    title: 'UX/UI Designer',
    matchScore: 81,
    description: 'Create intuitive and visually appealing user interfaces and experiences.',
    salaryRange: '$70,000 – $120,000',
    growthRate: '+13%',
    requiredSkills: ['Creativity', 'Prototyping', 'User Research', 'Figma'],
    education: "Bachelor's in Design or HCI",
    industry: 'Technology / Creative',
    workStyle: 'Remote',
    color: 'purple',
  },
  {
    id: 4,
    title: 'Product Manager',
    matchScore: 76,
    description: 'Lead product development from conception to launch, bridging business and tech.',
    salaryRange: '$100,000 – $180,000',
    growthRate: '+19%',
    requiredSkills: ['Leadership', 'Communication', 'Strategy', 'Agile'],
    education: "Bachelor's in Business or Engineering",
    industry: 'Technology',
    workStyle: 'On-site/Hybrid',
    color: 'green',
  },
  {
    id: 5,
    title: 'Research Scientist',
    matchScore: 72,
    description: 'Conduct scientific research and experiments to advance knowledge in a field.',
    salaryRange: '$75,000 – $130,000',
    growthRate: '+9%',
    requiredSkills: ['Research', 'Analytical Thinking', 'Writing', 'Statistics'],
    education: "PhD in relevant field",
    industry: 'Academia / R&D',
    workStyle: 'On-site',
    color: 'orange',
  },
];

const colorMap = {
  indigo: { badge: 'primary', bar: 'bg-indigo-500', border: 'border-l-indigo-500', icon: 'text-indigo-600 bg-indigo-50' },
  blue: { badge: 'info', bar: 'bg-blue-500', border: 'border-l-blue-500', icon: 'text-blue-600 bg-blue-50' },
  purple: { badge: 'purple', bar: 'bg-purple-500', border: 'border-l-purple-500', icon: 'text-purple-600 bg-purple-50' },
  green: { badge: 'success', bar: 'bg-green-500', border: 'border-l-green-500', icon: 'text-green-600 bg-green-50' },
  orange: { badge: 'warning', bar: 'bg-orange-400', border: 'border-l-orange-400', icon: 'text-orange-600 bg-orange-50' },
};

const CareerRecommendationPage = () => {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await assessmentService.getCareerRecommendations();
        setRecommendations(data);
      } catch {
        setRecommendations(MOCK_RECOMMENDATIONS);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>;
  if (!recommendations.length) return (
    <EmptyState title="No recommendations yet" description="Complete your assessments to receive personalized career recommendations." icon={Star} />
  );

  return (
    <div>
      <PageHeader title="Career Recommendations" subtitle={`${recommendations.length} career paths matched to your profile.`} />

      <div className="grid lg:grid-cols-3 gap-6">
        {/* List */}
        <div className="lg:col-span-1 space-y-3">
          {recommendations.map((rec) => {
            const c = colorMap[rec.color] || colorMap.indigo;
            return (
              <button
                key={rec.id}
                onClick={() => setSelected(rec)}
                className={`w-full text-left bg-white rounded-2xl border-2 border-l-4 p-4 transition-all hover:shadow-md ${
                  selected?.id === rec.id ? 'border-indigo-300 shadow-md' : 'border-gray-100'
                } ${c.border}`}
              >
                <div className="flex items-center justify-between mb-2">
                  <h3 className="font-semibold text-gray-900 text-sm">{rec.title}</h3>
                  <Badge variant={c.badge}>{rec.matchScore}% match</Badge>
                </div>
                <div className="w-full bg-gray-100 rounded-full h-1.5">
                  <div className={`${c.bar} h-1.5 rounded-full`} style={{ width: `${rec.matchScore}%` }} />
                </div>
                <p className="text-xs text-gray-500 mt-2">{rec.industry}</p>
              </button>
            );
          })}
        </div>

        {/* Detail */}
        <div className="lg:col-span-2">
          {selected ? (
            <Card>
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h2 className="text-2xl font-bold text-gray-900">{selected.title}</h2>
                  <p className="text-gray-500 text-sm mt-1">{selected.industry}</p>
                </div>
                <Badge variant={colorMap[selected.color]?.badge || 'primary'} size="md">
                  {selected.matchScore}% Match
                </Badge>
              </div>

              <p className="text-gray-600 mb-6 leading-relaxed">{selected.description}</p>

              <div className="grid sm:grid-cols-2 gap-4 mb-6">
                {[
                  { icon: DollarSign, label: 'Salary Range', value: selected.salaryRange, color: 'text-green-600 bg-green-50' },
                  { icon: TrendingUp, label: 'Growth Rate', value: selected.growthRate, color: 'text-blue-600 bg-blue-50' },
                  { icon: MapPin, label: 'Work Style', value: selected.workStyle, color: 'text-purple-600 bg-purple-50' },
                  { icon: BookOpen, label: 'Education', value: selected.education, color: 'text-orange-600 bg-orange-50' },
                ].map((item) => (
                  <div key={item.label} className={`flex items-center gap-3 p-3 rounded-xl ${item.color.split(' ')[1]}`}>
                    <item.icon size={18} className={item.color.split(' ')[0]} />
                    <div>
                      <p className="text-xs text-gray-500">{item.label}</p>
                      <p className="font-semibold text-gray-800 text-sm">{item.value}</p>
                    </div>
                  </div>
                ))}
              </div>

              <div>
                <h4 className="font-semibold text-gray-700 mb-3">Required Skills</h4>
                <div className="flex flex-wrap gap-2">
                  {selected.requiredSkills.map((skill) => (
                    <span key={skill} className="bg-indigo-50 text-indigo-700 text-xs font-medium px-3 py-1 rounded-full">
                      {skill}
                    </span>
                  ))}
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-gray-100">
                <Button>
                  Explore This Career Path <ChevronRight size={16} />
                </Button>
              </div>
            </Card>
          ) : (
            <div className="flex items-center justify-center h-full min-h-[300px] bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200">
              <div className="text-center">
                <Briefcase size={40} className="mx-auto text-gray-300 mb-3" />
                <p className="text-gray-400 font-medium">Select a career to see details</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CareerRecommendationPage;
